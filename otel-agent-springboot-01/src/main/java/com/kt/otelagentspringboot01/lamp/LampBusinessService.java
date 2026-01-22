package com.kt.otelagentspringboot01.lamp;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
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
