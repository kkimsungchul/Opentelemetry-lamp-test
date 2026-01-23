package com.kt.otelagentspringboot01.controller;



import com.kt.otelagentspringboot01.domain.LogApi;
import com.kt.otelagentspringboot01.service.LogService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
public class LogController {

    private final LogService logService;
    private static final Logger logger = LogManager.getLogger(LogController.class.getName());

    @PostMapping("/otel/v1/logs")
    public ResponseEntity<Void> receive(@RequestBody Map<String, Object> body) {
        System.out.println("==== RECEIVED LOG ====");
        System.out.println(body);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/save")
    public void save(){

        LogApi logApi = logService.save();
        logger.info("### save in ### {}",logApi.toString());
        logger.warn("### save in ### {}",logApi.toString());
        try {
            Thread.sleep(5000);
        }catch (Exception e){
            e.printStackTrace();
        }
        logService.update(logApi);
        logger.info("### save out ### {}",logApi.toString());
    }

    @GetMapping("")
    public List<LogApi> logApiList(){
        logger.info("### info save in ### logApiList");
        logger.warn("### warn save in ### logApiList");
        logger.info("### info save out ### logApiList");
        return logService.all();
    }
}
