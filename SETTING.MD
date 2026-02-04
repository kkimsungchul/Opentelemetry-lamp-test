## 목차
1. [애플리케이션 설정](#애플리케이션-설정)
2. [콜렉터 설정](#콜렉터-설정)
3. [플루언트 비트 설정](#플루언트-비트-설정)
4. [플루언트 비트 - wrap_message.lua](#플루언트-비트---wrap_messagelua-)


### 애플리케이션 설정

- 애플리케이션 구동 설정
- 오픈텔레메트리 에이전트를 애플리케이션 실행 시 세팅 함
- 애플리케이션에서 발생하는 모든 로그 수집
- OTEL_RESOURCE_ATTRIBUTES 옵션의 경우에는 , 로 구분해서 줘도됨

```shell
@echo off
rem Springboot build with gradle

echo ## gradlew.bat assemble

rem build
call gradlew.bat assemble
cd .\build\libs

rem env setting 
set JAVA_TOOL_OPTIONS=-javaagent:..\..\opentelemetry-javaagent.jar

set OTEL_SERVICE_NAME=otel-agent-springboot-01
set OTEL_RESOURCE_ATTRIBUTES=service.code=OG077201
set OTEL_RESOURCE_ATTRIBUTES=TOPIC_LAMP=TOPIC_LAMP02

set OTEL_METRIC_EXPORT_INTERVAL=1000
set OTEL_TRACES_EXPORTER=otlp
set OTEL_METRICS_EXPORTER=otlp
set OTEL_LOGS_EXPORTER=otlp

set OTEL_EXPORTER_OTLP_ENDPOINT=http://127.0.0.1:9999
set OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://127.0.0.1:9999
set OTEL_EXPORTER_OTLP_METRICS_ENDPOINT=http://127.0.0.1:9999
set OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=http://127.0.0.1:9999
set OTEL_EXPORTER_OTLP_PROTOCOL=grpc


java -jar otel-agent-springboot-01-0.0.1-SNAPSHOT.jar

rem Run SpringBoot end
rem echo ## cd .\build\libs&& java -jar otel-agent-springboot-01-0.0.1-SNAPSHOT.jar
```



### 콜렉터 설정
- 콜렉터 버전 : v0.144
- 다운로드 링크 : https://github.com/open-telemetry/opentelemetry-collector-releases/releases/tag/v0.144.0
- 수집된 로그 변환 및 전송을 담당함
- 수집된 LAMP규격의 로그를 json 타입으로 변환하여 해당 데이터 안에 trace id , span id , service name을 매핑

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 127.0.0.1:9999

#묶어서 보내지 않도록 설정
#다만 해도 묶어서 보냄..
processors:
  batch:
    timeout: 1ms
    send_batch_size: 1

# traceid와 spanid 를 속성에 매핑
  # logRecords flatten
  transform:
    log_statements:
      - context: log
        statements:
          # 1body.stringValue → body 로 단순화
          - set(body, body.string) where body.string != nil

          # 2trace / span 승격
          - set(attributes["trace_id"], trace_id.string)
          - set(attributes["span_id"], span_id.string)

          # 3TOPIC_LAMP → attributes (Kafka routing용)
          - set(attributes["topic"], resource.attributes["TOPIC_LAMP"])

          # 4OTEL 메타 제거
          - delete_key(resource.attributes, "*")
          - delete_key(attributes, "otel.*")


  transform/logs:
    log_statements:
      - context: log
        statements:
          # 1️ JSON 문자열만 파싱 (첫 글자가 '{')
          - set(body, ParseJSON(body.string))
            where IsString(body)
              and Substring(body.string, 0, 1) == "{"

          # 2️ trace / span 주입 (JSON 로그만)
          - set(body["trace_id"], trace_id.string)
            where IsMap(body)

          - set(body["span_id"], span_id.string)
            where IsMap(body)

          # 3️ topic 주입
          - set(body["topic"], resource.attributes["TOPIC_LAMP"])
            where IsMap(body)
          # 4 service_name 주입
          - set(body["service_name"], resource.attributes["service.name"])
            where IsMap(body)

          # 5 response 파싱(json)
          - set(body["response"], ParseJSON(body["response"]))
            where IsMap(body)
              and IsString(body["response"])
              and Substring(body["response"], 0, 1) == "{"

          # 6 security 파싱
          - set(body["security"], ParseJSON(body["security"]))
            where IsMap(body)
              and IsString(body["security"])
              and Substring(body["security"], 0, 1) == "{"

          # 7 security.detail 파싱
          - set(body["security"]["detail"], ParseJSON(body["security"]["detail"]))
            where IsMap(body)
              and IsString(body["security"]["detail"])
              and Substring(body["security"]["detail"], 0, 1) == "{"
 
          # 8 host 파싱
          - set(body["host"], ParseJSON(body["host"]))
            where IsMap(body)
              and IsString(body["host"])
              and Substring(body["host"], 0, 1) == "{"

exporters:
  # =====================
  # FILE EXPORTERS (분리)
  # =====================
  file/logs:
    path: logs.json

  file/traces:
    path: traces.json

  file/metrics:
    path: metrics.json

  # =====================
  # OTHER EXPORTERS
  # =====================
  debug:
    verbosity: detailed

  prometheus:
    endpoint: 127.0.0.1:9464

  otlp/jaeger:
    endpoint: 127.0.0.1:4317
    tls:
      insecure: true

  otlphttp:
    endpoint: http://127.0.0.1:3100/otlp

  otlphttp/fluentbit:
    endpoint: http://221.153.107.124:4318

  otlp/dataprepper:
    endpoint: 221.153.107.124:21890
    tls:
      insecure: true
  
  kafka/logs:
    brokers:
      - localhost:9092
    topic: TOPIC_LAMP05


service:
  pipelines:
    logs:
      receivers: [otlp]
      processors: [transform/logs]
      exporters: [file/logs, debug, otlphttp/fluentbit]
      #exporters: [file/logs, otlp/dataprepper, debug, kafka/logs]
      #exporters: [file/logs, otlphttp]

    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [file/traces]
      #exporters: [file/traces, otlp/jaeger]

    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [file/metrics]
      #exporters: [file/metrics, prometheus]

  telemetry:
    metrics:
      readers:
        - pull:
            exporter:
              prometheus:
                host: '0.0.0.0'
                port: 8888


```

### 플루언트 비트 설정

- 테스트를 진행하느라 주석들이 많지만, 해당 주석들은 추후 사용할 수 도 있어서 남겨둠
- 콜렉터에서 보내온 로그를 동적으로 카프카에 전달함
- 또한 lua 스크립트를 사용하여 데이터를 messgae 안으로 넣도록 함 ("wrap_message.lua"파일)

- 실행 순서

```
[INPUT: opentelemetry]
        |
        v
   Tag: otel.log
        |
        v
[FILTER: lua]  (Match parsed.log → SKIP)
        |
        v
[FILTER: rewrite_tag]
        |
        |-- timestamp 있음 → parsed.log 생성
        |-- otel.log 제거
        v
[FILTER 체인 재진입]
        |
        v
[FILTER: lua] (실행됨)
        |
        v
+---------------------+
| OUTPUT: parsing.log |
| OUTPUT: Kafka       |
+---------------------+
```


```
[SERVICE]
    Flush         1
    Log_Level     info
    Daemon        off
    Parsers_File  parsers.conf

[INPUT]
    Name          opentelemetry
    Listen        0.0.0.0
    Port          4318
    Tag    otel.log

[OUTPUT]
    Name          file
    Match         *
    Path          /home/sungchul/fluent-bit-log
    File          otel-logs.log

[OUTPUT]
    Name  file
    Match parsed.log
    Path  /home/sungchul/fluent-bit-log/
    File  parsing.log


[FILTER]
    Name    lua
    Match   parsed.log
    Script  /etc/fluent-bit/wrap_message.lua
    Call    wrap_message

[OUTPUT]
    Name        kafka
    Match       parsed.log
    Brokers     119.206.235.233:9092
    Topics      TOPIC_LAMP02, TOPIC_LAMP05
    Topic_Key   topic
    Dynamic_Topic On
    Format      json


#[FILTER]
#    Name    lua
#    Match   *
#    script  /etc/fluent-bit/extract_logs.lua
#    call    extract_log_records
#[OUTPUT]
#    Name   file
#    Match  *
#    Path   /home/sungchul/fluent-bit-log/
#    File  lua.log



#[FILTER]
#    Name    lua
#    Match   *
#    script  flatten.lua
#    call    flatten_otlp
#[OUTPUT]
#    Name   file
#    Match  *
#    Path   /home/sungchul/fluent-bit-log/
#    File flatten.log


#[FILTER]
#    Name              parser
#    Match             *
#    Key_Name          log
#    Parser            json_log
#    Reserve_Data      Off

#[FILTER]
#    Name   lua
#    Match  otel.log
#    Script /etc/fluent-bit/otel_attrs.lua
#    Call   extract_otel_attrs

#[FILTER]
#    Name          parser
#    Match         *
#    Key_Name      log
#    Parser        json_log
#    Reserve_Data  On


[FILTER]
    Name   rewrite_tag
    Match  *
    Rule   $timestamp ^.+$ parsed.log true

```


- 주석 작성된 내용

```
[SERVICE]
    Flush         1            # 출력 플러시 주기 (초). 1초마다 버퍼된 로그를 OUTPUT으로 전달
    Log_Level     info         # Fluent Bit 자체 로그 레벨 (info, debug, warn 등)
    Daemon        off          # 데몬 모드 비활성화 (foreground 실행)
    Parsers_File  parsers.conf # 파서 정의 파일 로드 (json, regex 파서 등)

[INPUT]
    Name          opentelemetry # OpenTelemetry OTLP 로그 입력 플러그인
    Listen        0.0.0.0       # 모든 네트워크 인터페이스에서 수신
    Port          4318          # OTLP HTTP 기본 포트
    Tag           otel.log      # 수신된 로그에 기본 태그 부여

[OUTPUT]
    Name          file         # 파일로 로그 출력
    Match         *            # 모든 태그의 로그 매칭
    Path          /home/sungchul/fluent-bit-log
    File          otel-logs.log # 원본 OTel 로그 전체를 파일로 저장 (디버깅/백업 용도)

[OUTPUT]
    Name          file         # 파일 출력 (두 번째 OUTPUT)
    Match         parsed.log   # parsed.log 태그만 매칭
    Path          /home/sungchul/fluent-bit-log/
    File          parsing.log  # 파싱/가공된 로그만 별도 파일로 저장

[FILTER]
    Name    lua                # Lua 스크립트 기반 필터
    Match   parsed.log         # parsed.log 태그에만 적용
    Script  /etc/fluent-bit/wrap_message.lua
    Call    wrap_message       # Lua 함수 호출
                               # → 로그 구조 변환, 필드 추출, message 감싸기/풀기 등에 사용

[OUTPUT]
    Name        kafka          # Kafka 출력 플러그인
    Match       parsed.log     # 파싱된 로그만 Kafka로 전송
    Brokers     119.206.235.233:9092  # Kafka 브로커 주소
    Topics      TOPIC_LAMP02, TOPIC_LAMP05   # 기본적으로 사용 가능한 토픽 목록
                               
    Topic_Key   topic          # 로그 레코드 내 'topic' 필드 값을 기준으로 토픽 선택
    Dynamic_Topic On           # 레코드마다 동적으로 Kafka 토픽 결정
    Format      json           # Kafka로 JSON 형태로 전송

[FILTER]
    Name   rewrite_tag         # 태그 재작성 필터
    Match  *                   # 모든 태그 대상
    Rule   $timestamp ^.+$ parsed.log true
                               # timestamp 필드가 존재하면
                               # → 태그를 parsed.log 로 변경
                               # → true: 원본 로그는 제거하고 새 태그만 유지

```





### 플루언트 비트 - wrap_message.lua 

- 데이터를 한번더 message 로 감싸도록 하는 스크립트

```js
function wrap_message(tag, timestamp, record)
    local new_record = {}
    new_record["message"] = record
    return 1, timestamp, new_record
end
```