package com.kt.otelagentspringboot01.lamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LampService {

    public LampVO createSampleLoginLog() throws Exception{
        LampVO vo = new LampVO();

        // =====================
        // 공통 값
        // =====================
        vo.setTimestamp(
                LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        );
        vo.setService("OG077201");
        vo.setOperation("login");
        vo.setTransactionId(UUID.randomUUID().toString());
        vo.setLogType("IN_RES");

        String serverIp = getServerIp();

        // =====================
        // Host
        // =====================
        LampVO.HostVo host = vo.new HostVo();
        host.setName(InetAddress.getLocalHost().getHostName());
        host.setIp(serverIp);
        vo.setHost(host);

        // =====================
        // Response
        // =====================
        LampVO.ResponseVo response = vo.new ResponseVo();
        response.setType("I");
        response.setCode("SUCCESS");
        response.setDuration("303");
        vo.setResponse(response);

        // =====================
        // User
        // =====================
        LampVO.UserVo user = vo.new UserVo();
        user.setId("82284884");
        user.setIp(serverIp);
        user.setType("LAMP_MANAGER");
        vo.setUser(user);

        // =====================
        // Security
        // =====================
        LampVO.SecureVo security = vo.new SecureVo();
        security.setType("ACCESS");
        security.setEvent("LOGIN");
        security.setTarget("82284884");
        security.setReason("잘되나 테스트 해보자");
        security.setDetail(
                "{\"id\":\"82284884\",\"ip\":\"10.225.164.84\",\"type\":\"LAMP_MANAGER\"}"
        );
        vo.setSecurity(security);

        return vo;
    }

    private String getServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    public String logToJson(LampVO lampLog) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(lampLog);
        return json;
    }

}
