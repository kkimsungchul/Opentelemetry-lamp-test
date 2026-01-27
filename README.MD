## 목차
1. [아키텍처 구성](#아키텍처-구성)
2. [실행 방법](#실행-방법)
3. [테스트 URL](#테스트-url)
4. [로그 저장 위치](#로그-저장-위치)
5. [툴 설명](#툴-설명)
  - [애플리케이션](#애플리케이션)
  - [오픈텔레메트리 에이전트](#오픈텔레메트리-에이전트)
  - [오픈텔레메트리 콜렉터](#오픈텔레메트리-콜렉터)
  - [오픈텔레메트리 콜렉터 컨트리브](#오픈텔레메트리-콜렉터-컨트리브)
  - [데이터 프레퍼](#데이터-프레퍼data-prepper)
6. [오픈텔레메트리 에이전트를 사용한 구동](#오픈텔레메트리-에이전트를-사용한-구동)
7. [JAVA 소스코드](#java-소스코드)
8. [발생시킨 로그 원문](#발생시킨-로그-원문)
9. [오픈텔레메트리 콜렉터 수집 로그 규격](#오픈텔레메트리-콜렉터-수집-로그-규격)
10. [오픈텔레메트리 콜렉터 설정 (데이터 프레퍼로 전송)](#오픈텔레메트리-콜렉터-설정-데이터-프레퍼로-전송)
11. [수집되는 로그 규격 비교](#수집되는-로그-규격-비교)
13. [데이터 프레퍼에서 로그 변환](#데이터-프레퍼에서-로그-변환)
14. [실제 로그 데이터 위치](#실제-로그-데이터-위치)


# fluent-bit 내용은 아래 파일 참고

[fluent-bit 사용을 위한 전체 설정파일](./SETTING.MD)

[fluent-bit 설치 및 기본 사용](https://github.com/kkimsungchul/study/blob/master/Observability/%5Bfluentbit%5D%20rocky%EC%97%90%20fluentbit%20%EC%84%A4%EC%B9%98.txt)

[fluent-bit 에서 데이터 파싱하기](https://github.com/kkimsungchul/study/blob/master/Observability/%5Bfluentbit%5D%20fluentbit%EC%97%90%EC%84%9C%20%EB%8D%B0%EC%9D%B4%ED%84%B0%20%ED%8C%8C%EC%8B%B1.txt)

[fluent-bit 에서 trace id 접근 테스트](https://github.com/kkimsungchul/study/blob/master/Observability/%5Bfluentbit%5D%20fluentbit%EC%97%90%EC%84%9C%20traceId%20%2C%20spanId%20%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0%20-%20%EB%B6%88%EA%B0%80%EB%8A%A5.txt)

[fluent-bit vs dataprepper](https://github.com/kkimsungchul/study/blob/master/Observability/%5BOTel%5D%20Opentelemetry%EC%9D%98%20%EB%8D%B0%EC%9D%B4%ED%84%B0%20%EC%B2%98%EB%A6%AC%20%ED%88%B4%20%EC%84%A0%ED%83%9D.txt)

[fluent-bit 에서 kafka 토픽 동적으로 설정하기](https://github.com/kkimsungchul/study/blob/master/Observability/%5Bfluentbit%5D%20fluentbit%EC%97%90%EC%84%9C%20%EB%8F%99%EC%A0%81%EC%9C%BC%EB%A1%9C%20kafka%ED%86%A0%ED%94%BD%20%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0.txt)

[fluent-bit 수집 로그](./log_data/fluent-bit-logs.json)

### 아키텍처 구성
아래 순서로 로그 데이터 흐름
```
애플리케이션(오픈텔레메트리 에이전트)
-> 오픈텔레메트리 콜렉터
-> 데이터 프레퍼
-> json 파일
```
여기서 json 파일은 나중에 카프카로 변경하면됨
각각 구성위치는
- 애플리케이션 -> 윈도우
- 오픈텔레메트리 콜렉터 -> 윈도우
- 데이터 프레퍼 -> 리눅스

### 실행 방법

- 오픈텔레메트리 콜렉터
  ```
  start_otelcol.bat
  ```

- 애플리케이션
  ```
  cd otel-agent-springboot-01
  start_otelcol.bat
  ```

### 테스트 URL

- http://localhost:5050/lamp
```
LAMP로그만 생성해서 테스트함
```

- http://localhost:5050/log-save
```
LAMP로그가 아닌 그냥 자체 로그를 저장하도록 하는 로직
```
- http://localhost:5050/log
```
log테이블에 저장된 로그를 불러오는 로직
프로젝트 실행 후 바로 실행할경우 log테이블에는 아무런 데이터가 없기 때문에 오류가 발생함
logApi = logApis.get(logApis.size() - 1);
```

### 로그 저장 위치

- 매트릭 : 최상위경로/metrics.json
- 트레이스 : 최상위경로/traces.json
- 로그 : 최상위경로/logs.json


### 툴 설명

- 애플리케이션 :
  - LAMP 로그 규격을 발생 시킬 애플리케이션
  - 애플리케이션 실행 시 오픈텔레메트리 에이전트를 같이 실행하도록 설정함

- 오픈텔레메트리 에이전트 :
  - 애플리케이션에서 발생하는 로그,트레이스,매트릭을 콜렉터로 전송함
  - 테스트에서는 아래 옵션으로 해서 실행하였음

- 오픈텔레메트리 콜렉터 :
  - 오픈텔레메트리에서 수집,변환,전송을 담당함
  - 리시버 , 프로세서, 익스포터로 구성되어 있음
  - 리시버는 수집
  - 프로세서는 수집한 데이터를 필터/변환/샘플링 가능
  - 익스포터는 어디로 보낼지 정할 수 있으며, 한곳으로만 보내는게 아닌 로그를 파일/카프카/로키등으로 여러개로 전송 가능

- 오픈텔레메트리 콜렉터 컨트리브
  - 오픈텔레메트리 콜렉터는 공식에서 제공하는 기본수집기지만, 커트리브는 추가 컴포넌트들을 포함하고 있음
  - 익스포터, 프로세서 확장 기능을 사용하기 위해 사용함
  - 다운로드 링크에서 다운 받은 후 콜렉터와 똑같이 설정파일을 만들어주고 실행하면 됨
  - 다운로드 링크 : https://github.com/open-telemetry/opentelemetry-collector-releases/releases/tag/v0.144.0

  - 기본 콜렉터와 컨트리브의 차이점

  | 구분 | Collector (Core) | Collector Contrib |
  |---------------------|-----------------------------------------------|------------------------------------------------|
  | **포함 컴포넌트** | 최소 기본 Receiver / Exporter | Core + 커뮤니티/벤더 확장 컴포넌트 포함 |
  | **안정성** | 매우 높음 | 컴포넌트별 안정성 상이 |
  | **빌드 크기** | 작음 | 큼 |
  | **용도** | 표준 OTLP 통신, 경량 배포 | 다양한 로그·메트릭·트레이스 수집 가능 |
  | **유지보수** | OpenTelemetry 공식 팀 직접 관리 | 일부는 커뮤니티 또는 벤더에서 유지 |

  - OpenTelemetry Collector (Core)
  - **기본 버전**
    - 필수 Receiver / Processor / Exporter만 포함
    - 예: OTLP, Jaeger, Prometheus 등
    - **특징**
      - 가볍고 안정적
      - 보안 검증이 비교적 철저함
      - 외부 시스템 연동 기능이 제한적
  
  - OpenTelemetry Collector Contrib
  - **확장 버전**
    - Core에 없는 Receiver·Exporter 포함
    - 예: Kafka, AWS X-Ray, Azure Monitor, Redis, MySQL 등
    - **특징**
      - 다양한 환경 지원
      - 기능은 많지만 빌드 크기 증가 및 의존성 많음
      - 일부 컴포넌트는 안정성·지원 주기 차이 있음


- 데이터 프레퍼(data prepper)
  - 오픈텔레메트리에서 수집한 로그들의 전처리를 담당함
  - 오픈텔레메트리 로그에는 "행(레코드)" 개념이 없음
  - 사용자의 하나의 호출에서 발생한 로그는 다 하나로 기록됨.
  - 이부분은 아래에서 작성함
  - 윈도우에는 설치가 불가능하니 아래 링크를 참고해서 리눅스에 직접 구축해서 연동함
  - https://github.com/kkimsungchul/study/blob/master/linux/%5Blinux%5D%20rocky%EC%97%90%20dataprepper%20%EC%84%A4%EC%B9%98.txt


### 오픈텔레메트리 에이전트를 사용한 구동

- 파일명 : otel-agent-springboot-01/start_agent_springboot.bat
- 속성을 추가로 주고싶다면 OTEL_RESOURCE_ATTRIBUTES=service.code=OG077201 이런식으로 추가하면 됨
```shell
OTEL_RESOURCE_ATTRIBUTES=service.code=OG077201
OTEL_RESOURCE_ATTRIBUTES=service.host=192.168.5.100
OTEL_RESOURCE_ATTRIBUTES=service.rank=C
```
- 혹시나 해서 적어놓지만 OTEL_SERVICE_NAME는 오텔에서 기본적으로 제공해주는 속성임, 아래와 같이 맘대로 쓰지 말것
```
OTEL_SERVICE_CODE = OG077201
OTEL_SERVICE_HOST = 192.168.5.100
```

- 아래는 프로젝트 내에 있는 스프링부트 실행 시 준 환경변수 설정임
- 리눅스도 변수명은 똑같으니 컨버팅해서 사용하면 됨
```shell
@echo off
rem Springboot build with gradle

echo ## gradlew.bat assemble

call gradlew.bat assemble
cd .\build\libs

set JAVA_TOOL_OPTIONS=-javaagent:..\..\opentelemetry-javaagent.jar

set OTEL_SERVICE_NAME=otel-agent-springboot-01
set OTEL_RESOURCE_ATTRIBUTES=service.code=OG077201

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

rem Run SpringBoot
rem echo ## cd .\build\libs&& java -jar otel-agent-springboot-01-0.0.1-SNAPSHOT.jar
```


### JAVA 소스코드

- LampService 에서는 LAMP 로그 규격에 맞는 로그만 생성해줘서 아래에는 작성하지 않았음
- 소스코드 확인이 필요하면 otel-agent-springboot-01 로 가서 확인하면됨
- 내장 DB를 사용하도록 해놓아서 DB사용 관련은 아래 패키지 경로 밑의 소스코드내에서 확인하면됨
```
otel-agent-springboot-01\src\main\java\com\kt\otelagentspringboot01\lamp\
```

- 컨트롤러
  ```java
  public class LampController {

      private static final Logger logger = LogManager.getLogger(LampController.class.getName());
      private final LampService lampService;
      private final LampBusinessService lampBusinessService;

      @GetMapping("")
      public LampVO lamp() throws Exception{
          LampVO lampVO = lampService.createSampleLoginLog();
          lampVO.setOperation("LampController Logic");
          String strLog = lampService.logToJson(lampVO);
          logger.info(strLog);
          lampBusinessService.serviceLogic();
          return lampVO;
      }
  }
  ```

- 서비스
  ```java
  public class LampBusinessService {

      private final LampService lampService;

      private static final Logger logger = LogManager.getLogger(LampBusinessService.class.getName());

      public String serviceLogic() throws Exception{
          LampVO lampVO = lampService.createSampleLoginLog();
          lampVO.setOperation("LampBusinessService Logic");
          String strLog = lampService.logToJson(lampVO);
          logger.info(strLog);

          return strLog;
      }
  }
  ```

### 발생시킨 로그 원문

- 아래 로그는 애플리케이션을 실행해서 직접 발생시킨 로그임
```json
{
  "timestamp": "2026-01-22 23:14:22.750",
  "service": "OG077201",
  "operation": "LampController Logic",
  "bizTransactionId": null,
  "transactionId": "f21b5e85-f61d-41e9-8d15-ed8c763c8986",
  "logType": "IN_RES",
  "payload": null,
  "caller": null,
  "host": {
    "name": "sung-PC",
    "ip": "169.254.83.107"
  },
  "response": {
    "type": "I",
    "code": "SUCCESS",
    "duration": "303"
  },
  "user": {
    "id": "82284884",
    "ip": "169.254.83.107",
    "type": "LAMP_MANAGER"
  },
  "device": null,
  "security": {
    "type": "ACCESS",
    "event": "LOGIN",
    "target": "82284884",
    "reason": "잘되나 테스트 해보자",
    "detail": "{\"id\":\"82284884\",\"ip\":\"10.225.164.84\",\"type\":\"LAMP_MANAGER\"}"
  },
  "destination": null
}
```


### 오픈텔레메트리 콜렉터 수집 로그 규격
- 오픈텔레메트리 에이전트를 사용하여 수집된 로그는 모두 아래 규격으로 수집됨
- 여기서 중요한점은 "행(row)" 개념이 없이 일정시간 또는 일정 개수를 모아서 처리한다는 것임
- 데이터는 아래구조로 되어있으며, 우리가 원하는 데이터는 logRecords 안에 body안에 키:벨류 형태로 들어가있음

```json
{
  "resourceLogs": [
    {
      "resource": {...},
      "scopeLogs": [
        {
          "scope": {...},
          "logRecords": [
            { /* log 1 */ },
            { /* log 2 */ },
            { /* log 3 */ }
          ]
        }
      ]
    }
  ]
}
```

- 로그를 2번 찍어도 오픈텔레메트리 콜렉터에서 묶어서 보내기때문에 logRecords 안에 모든 데이터가 들어가게 됨
- 이거를 분리하려고 해도 오픈텔레메트리 콜렉터 내부 버퍼가 존재하여 일정 시간 / 개수 단위로 묶어서 보내게 됌
- Storm에서 for 문을 돌면서 처리를 해도 되지만, 이럴경우 부하가 갈 가능성이 크니 분리하는 작업이 필요함

  ```json
  {
    "resourceLogs": [
      {
        "resource": {
          "attributes": [
            {
              "key": "host.arch",
              "value": {
                "stringValue": "amd64"
              }
            },
            {
              "key": "host.name",
              "value": {
                "stringValue": "sung-PC"
              }
            },
            {
              "key": "os.description",
              "value": {
                "stringValue": "Windows 10 10.0"
              }
            },
            {
              "key": "os.type",
              "value": {
                "stringValue": "windows"
              }
            },
            {
              "key": "process.command_line",
              "value": {
                "stringValue": "C:\\Program Files\\Java\\jdk-17.0.2\\bin\\java.exe -javaagent:..\\..\\opentelemetry-javaagent.jar -jar otel-agent-springboot-01-0.0.1-SNAPSHOT.jar"
              }
            },
            {
              "key": "process.executable.path",
              "value": {
                "stringValue": "C:\\Program Files\\Java\\jdk-17.0.2\\bin\\java.exe"
              }
            },
            {
              "key": "process.pid",
              "value": {
                "intValue": "21012"
              }
            },
            {
              "key": "process.runtime.description",
              "value": {
                "stringValue": "Oracle Corporation OpenJDK 64-Bit Server VM 17.0.2+8-86"
              }
            },
            {
              "key": "process.runtime.name",
              "value": {
                "stringValue": "OpenJDK Runtime Environment"
              }
            },
            {
              "key": "process.runtime.version",
              "value": {
                "stringValue": "17.0.2+8-86"
              }
            },
            {
              "key": "service.code",
              "value": {
                "stringValue": "OG077201"
              }
            },
            {
              "key": "service.instance.id",
              "value": {
                "stringValue": "8b7e6178-0f97-4ae4-8cae-56cac18d56fb"
              }
            },
            {
              "key": "service.name",
              "value": {
                "stringValue": "otel-agent-springboot-01"
              }
            },
            {
              "key": "service.version",
              "value": {
                "stringValue": "0.0.1-SNAPSHOT"
              }
            },
            {
              "key": "telemetry.distro.name",
              "value": {
                "stringValue": "opentelemetry-java-instrumentation"
              }
            },
            {
              "key": "telemetry.distro.version",
              "value": {
                "stringValue": "2.3.0"
              }
            },
            {
              "key": "telemetry.sdk.language",
              "value": {
                "stringValue": "java"
              }
            },
            {
              "key": "telemetry.sdk.name",
              "value": {
                "stringValue": "opentelemetry"
              }
            },
            {
              "key": "telemetry.sdk.version",
              "value": {
                "stringValue": "1.37.0"
              }
            }
          ]
        },
        "scopeLogs": [
          {
            "scope": {
              "name": "com.kt.otelagentspringboot01.lamp.LampController"
            },
            "logRecords": [
              {
                "timeUnixNano": "1769085883083204300",
                "observedTimeUnixNano": "1769085883083204300",
                "severityNumber": 9,
                "severityText": "INFO",
                "body": {
                  "stringValue": "{\"timestamp\":\"2026-01-22 21:44:42.985\",\"service\":\"OG077201\",\"operation\":\"LampController Logic\",\"bizTransactionId\":null,\"transactionId\":\"af34d533-a5fc-40a1-984c-2ac2e26ff0ab\",\"logType\":\"IN_RES\",\"payload\":null,\"caller\":null,\"host\":{\"name\":\"sung-PC\",\"ip\":\"169.254.83.107\"},\"response\":{\"type\":\"I\",\"code\":\"SUCCESS\",\"duration\":\"303\"},\"user\":{\"id\":\"82284884\",\"ip\":\"169.254.83.107\",\"type\":\"LAMP_MANAGER\"},\"device\":null,\"security\":{\"type\":\"ACCESS\",\"event\":\"LOGIN\",\"target\":\"82284884\",\"reason\":\"잘되나 테스트 해보자\",\"detail\":\"{\\\"id\\\":\\\"82284884\\\",\\\"ip\\\":\\\"10.225.164.84\\\",\\\"type\\\":\\\"LAMP_MANAGER\\\"}\"},\"destination\":null}"
                },
                "flags": 1,
                "traceId": "cc1399e0d28b722ae64dd0c68e2a80b7",
                "spanId": "71e95e338ae0a936"
              }
            ]
          },
          {
            "scope": {
              "name": "com.kt.otelagentspringboot01.lamp.LampBusinessService"
            },
            "logRecords": [
              {
                "timeUnixNano": "1769085883091784000",
                "observedTimeUnixNano": "1769085883091784000",
                "severityNumber": 9,
                "severityText": "INFO",
                "body": {
                  "stringValue": "{\"timestamp\":\"2026-01-22 21:44:43.087\",\"service\":\"OG077201\",\"operation\":\"LampBusinessService Logic\",\"bizTransactionId\":null,\"transactionId\":\"d7b44411-3e58-4dde-aa4f-9398ce363153\",\"logType\":\"IN_RES\",\"payload\":null,\"caller\":null,\"host\":{\"name\":\"sung-PC\",\"ip\":\"169.254.83.107\"},\"response\":{\"type\":\"I\",\"code\":\"SUCCESS\",\"duration\":\"303\"},\"user\":{\"id\":\"82284884\",\"ip\":\"169.254.83.107\",\"type\":\"LAMP_MANAGER\"},\"device\":null,\"security\":{\"type\":\"ACCESS\",\"event\":\"LOGIN\",\"target\":\"82284884\",\"reason\":\"잘되나 테스트 해보자\",\"detail\":\"{\\\"id\\\":\\\"82284884\\\",\\\"ip\\\":\\\"10.225.164.84\\\",\\\"type\\\":\\\"LAMP_MANAGER\\\"}\"},\"destination\":null}"
                },
                "flags": 1,
                "traceId": "cc1399e0d28b722ae64dd0c68e2a80b7",
                "spanId": "71e95e338ae0a936"
              }
            ]
          },
          {
            "scope": {
              "name": "org.apache.catalina.core.ContainerBase.[Tomcat].[localhost].[/]"
            },
            "logRecords": [
              {
                "timeUnixNano": "1769085882915000000",
                "observedTimeUnixNano": "1769085882915204200",
                "severityNumber": 9,
                "severityText": "INFO",
                "body": {
                  "stringValue": "Initializing Spring DispatcherServlet 'dispatcherServlet'"
                },
                "flags": 1,
                "traceId": "cc1399e0d28b722ae64dd0c68e2a80b7",
                "spanId": "71e95e338ae0a936"
              }
            ]
          },
          {
            "scope": {
              "name": "org.springframework.web.servlet.DispatcherServlet"
            },
            "logRecords": [
              {
                "timeUnixNano": "1769085882915000000",
                "observedTimeUnixNano": "1769085882915204200",
                "severityNumber": 9,
                "severityText": "INFO",
                "body": {
                  "stringValue": "Initializing Servlet 'dispatcherServlet'"
                },
                "flags": 1,
                "traceId": "cc1399e0d28b722ae64dd0c68e2a80b7",
                "spanId": "71e95e338ae0a936"
              },
              {
                "timeUnixNano": "1769085882918000000",
                "observedTimeUnixNano": "1769085882918203000",
                "severityNumber": 9,
                "severityText": "INFO",
                "body": {
                  "stringValue": "Completed initialization in 1 ms"
                },
                "flags": 1,
                "traceId": "cc1399e0d28b722ae64dd0c68e2a80b7",
                "spanId": "71e95e338ae0a936"
              }
            ]
          }
        ],
        "schemaUrl": "https://opentelemetry.io/schemas/1.24.0"
      }
    ]
  }
  ```

### 오픈텔레메트리 콜렉터 설정 (데이터 프레퍼로 전송)
- 콜렉터에서 수집한 로그를 데이터 프레퍼로 보내서 전처리를 하기 위함
- 아래 설정에서 otlp/dataprepper를 추가하고 logs: exporters: 에 넣어 주었음
- traceid와 spanid 를 속성에 매핑 하는부분은 혹시나 속성값을 나중에 쓸 수 있으니 작성 했음, 제거하여도 상관없는 값
```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 127.0.0.1:9999
      http:
        endpoint: 127.0.0.1:4318

# 일정시간 또는 개수만큼 묶어서 전송
#processors:
#  batch:


#묶어서 보내지 않도록 설정
#다만 해도 묶어서 보냄..
processors:
  batch:
    timeout: 0s
  # traceid와 spanid 를 속성에 매핑
  transform:
    log_statements:
      - context: log
        statements:
          - set(attributes["trace_id"], trace_id.string)
          - set(attributes["span_id"], span_id.string)
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
  logging:
    verbosity: detailed

  prometheus:
    endpoint: 127.0.0.1:9464

  otlp/jaeger:
    endpoint: 127.0.0.1:4317
    tls:
      insecure: true

  otlphttp:
    endpoint: http://127.0.0.1:3100/otlp

  otlp/dataprepper:
    endpoint: 이건내진짜서버아이피라서안됨:21890
    tls:
      insecure: true

service:
  pipelines:
    logs:
      receivers: [otlp]
      processors: [batch]
      exporters: [file/logs, otlp/dataprepper]
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
```

### 수집되는 로그 규격 비교
- 오텔 수집 로그
```
{
  "resourceLogs": [
    {
      "resource": {...},
      "scopeLogs": [
        {
          "scope": {...},
          "logRecords": [
            { /* log 1 */ },
            { /* log 2 */ },
            { /* log 3 */ }
          ]
        }
      ]
    }
  ]
}
```

- 데이터 프레퍼 처리 로그
```
{"traceId":"9d3a14f02ebc4184dd6d8f6e86a70b06","spanId":"9092cdd6d54cee8a","body":"### info get in ### logApiList","ob
{"traceId":"9d3a14f02ebc4184dd6d8f6e86a70b06","spanId":"9092cdd6d54cee8a","body":"### warn get in ### logApiList","ob
{"traceId":"9d3a14f02ebc4184dd6d8f6e86a70b06","spanId":"9092cdd6d54cee8a","body":"{\"timestamp\":\"2026-01-23 00:22:0
```

### 데이터 프레퍼에서 로그 변환

- 위 데이터를 바로 사용해도 되지만, 원하는건 body안에 있는 json 타입의 데이터
- 설정파일 변경 (pipelines.yaml)
```yaml
body-only-pipeline:
  source:
    otel_logs_source:
      ssl: false
      port: 21890

  processor:
    # 1.  body →> message로 변환
    - rename_keys:
        entries:
          - from_key: body
            to_key: message

    # 2. message를 JSON으로 파싱
    - parse_json:
        source: message
        destination: message
    # 3. message 필드에 trace_id와 span_id추가
    - add_entries:
        entries:
          - key: message/trace_id
            format: "${traceId}"
          - key: message/span_id
            format: "${spanId}"

  sink:
    - file:
        path: /home/dataprepper/log/lamp-logs.json
        append: true

```

- 출력 데이터
```json
	{
	  "traceId": "ac21153b7a5579fd1a63ef7e0659379b",
	  "spanId": "264630ec38feb394",
	  "message": {
		"logType": "IN_RES",
		"destination": null,
		"transactionId": "af0c76a2-77fc-4d36-b570-11448bab7f3d",
			....중략...
		"timestamp": "2026-01-26 22:32:21.533",
		"trace_id": "ac21153b7a5579fd1a63ef7e0659379b",
		"span_id": "264630ec38feb394"
	  },
	  "log.attributes.trace_id": "ac21153b7a5579fd1a63ef7e0659379b",
	  "resource.attributes.telemetry@sdk@language": "java",
	  "resource.attributes.host@name": "sung-PC",
	  "resource.attributes.process@pid": 24636,
	  "resource.attributes.host@arch": "amd64",
	  "resource.attributes.process@runtime@description": "Oracle Corporation OpenJDK 64-Bit Server VM 17.0.2+8-86",
	  "resource.attributes.process@executable@path": "C:\\Program Files\\Java\\jdk-17.0.2\\bin\\java.exe",
	  "resource.attributes.service@instance@id": "cbf5dd2e-321a-4e38-90c0-558a337eea9b",
	  "resource.attributes.telemetry@sdk@version": "1.37.0",
	  "resource.attributes.service@name": "otel-agent-springboot-01",
	  "resource.attributes.process@command_line": "C:\\Program Files\\Java\\jdk-17.0.2\\bin\\java.exe -javaagent:..\\..\\opentelemetry-javaagent.jar -jar otel-agent-springboot-01-0.0.1-SNAPSHOT.jar",
	  "instrumentationScope.name": "com.kt.otelagentspringboot01.lamp.LampBusinessService",
	  "resource.attributes.process@runtime@version": "17.0.2+8-86",
	  "resource.attributes.service@version": "0.0.1-SNAPSHOT",
	  "resource.attributes.telemetry@sdk@name": "opentelemetry",
	  "resource.attributes.process@runtime@name": "OpenJDK Runtime Environment",
	  "resource.attributes.telemetry@distro@name": "opentelemetry-java-instrumentation",
	  "log.attributes.span_id": "264630ec38feb394",
	  "resource.attributes.os@type": "windows",
	  "resource.attributes.TOPIC_LAMP": "OG077201",
	  "resource.attributes.os@description": "Windows 10 10.0",
	  "resource.attributes.telemetry@distro@version": "2.3.0"
	}	
```


### 실제 로그 데이터 위치
[오픈텔레메트리 콜렉터 로그](./log_data/dataprepper-lamp-logs.json)

[데이터프레퍼 로그](./log_data/opentelemetry-logs.json)

[플루언트비트 로그](./log_data/fluent-bit-logs.json)