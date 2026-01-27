## 목차
1. [애플리케이션 설정](#애플리케이션-설정)
2. [콜렉터 설정](#콜렉터-설정)
3. [플루언트 비트 설정](#플루언트-비트-설정)
4. [플루언트 비트 - wrap_message.lua](#플루언트-비트---wrap_messagelua-)


### 애플리케이션 설정

- 애플리케이션 구동 설정
- 오픈텔레메트리 에이전트를 애플리케이션 실행 시 세팅 함
- 애플리케이션에서 발생하는 모든 로그 수집
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
          # 1JSON 문자열만 파싱 (첫 글자가 '{')
          - set(body, ParseJSON(body.string))
            where IsString(body)
              and Substring(body.string, 0, 1) == "{"
          # 2trace / span 주입 (JSON 로그만)
          - set(body["trace_id"], trace_id.string)
            where IsMap(body)
          - set(body["span_id"], span_id.string)
            where IsMap(body)
          # 3topic 주입
          - set(body["topic"], resource.attributes["TOPIC_LAMP"])
            where IsMap(body)
          # 4service_name 주입
          - set(body["service_name"], resource.attributes["service.name"])
            where IsMap(body)

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

[FILTER]
    Name    lua
    Match   parsed.log
    Script  /etc/fluent-bit/wrap_message.lua
    Call    wrap_message

[OUTPUT]
    Name  file
    Match parsed.log
    Path  /home/sungchul/fluent-bit-log/
    File  parsing.log

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

#[FILTER]
#    Name modify
#    Match parsed.log
#    Copy topic topic

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