package com.kt.otelagentspringboot01.lamp;


import com.kt.otelagentspringboot01.domain.LogApi;
import com.kt.otelagentspringboot01.service.LogService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/lamp")
public class LampController {

    private static final Logger logger = LogManager.getLogger(LampController.class.getName());
    private final LampService lampService;
    private final LampBusinessService lampBusinessService;
    private final LogService logService;

    @GetMapping("")
    public LampVO lamp() throws Exception{
        logger.info("### LampController start");
        LampVO lampVO = lampService.createSampleLoginLog();
        lampVO.setOperation("LampController Logic");
        String strLog = lampService.logToJson(lampVO);
        logger.info(strLog);
        lampBusinessService.serviceLogic();
        logger.info("### LampController end");
        return lampVO;
    }

    @GetMapping("/log-save")
    public String test()throws Exception{
        LogApi logApi = new LogApi();
        logApi = logService.save();
        LampVO lampVO = lampService.createSampleLoginLog();
        lampVO.setPayload(logApi.toString());
        String strLog = lampService.logToJson(lampVO);
        logger.info(strLog);
        return strLog;
    }

    @GetMapping("/log")
    public String test2() throws Exception{
        logger.info("### info get in ### logApiList");
        logger.warn("### warn get in ### logApiList");
        logger.debug("### debug get out ### logApiList");
        LogApi logApi = new LogApi();
        List<LogApi> logApis= logService.all();

        logApi = logApis.get(logApis.size() - 1);
        LampVO lampVO = lampService.createSampleLoginLog();
        lampVO.setPayload(logApi.toString());
        String strLog = lampService.logToJson(lampVO);
        logger.info(strLog);
        return strLog;

    }


}
