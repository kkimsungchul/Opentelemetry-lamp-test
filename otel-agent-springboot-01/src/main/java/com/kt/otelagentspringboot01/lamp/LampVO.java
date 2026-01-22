package com.kt.otelagentspringboot01.lamp;

import com.fasterxml.jackson.annotation.JsonInclude;

public class LampVO {
    private String timestamp;
    private String service;
    private String operation;
    private String bizTransactionId;
    private String transactionId;
    private String logType;
    private String payload;
    //private Object payload;

    private CallerVo caller;
    private HostVo host;
    private ResponseVo response;
    private UserVo user;
    private DeviceVo device;
    private SecureVo security;
    private DestinationVo destination;

    public <T> T createInstance(Class<T> clz) {
        T t = null;
        if(clz.equals(UserVo.class)) t = (T) new UserVo();
        else if(clz.equals(CallerVo.class)) t = (T) new CallerVo();
        else if(clz.equals(HostVo.class)) t = (T) new HostVo();
        else if(clz.equals(ResponseVo.class)) t = (T) new ResponseVo();
        else if(clz.equals(DeviceVo.class)) t = (T) new DeviceVo();
        else if(clz.equals(SecureVo.class)) t = (T) new SecureVo();
        else if(clz.equals(DestinationVo.class)) t = (T) new DestinationVo();
        return t;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    public String getBizTransactionId() {
        return bizTransactionId;
    }
    public void setBizTransactionId(String bizTransactionId) {
        this.bizTransactionId = bizTransactionId;
    }
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getLogType() {
        return logType;
    }
    public void setLogType(String logType) {
        this.logType = logType;
    }
    public String getPayload() {
        return payload;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }
    public CallerVo getCaller() {
        return caller;
    }
    public void setCaller(CallerVo caller) {
        this.caller = caller;
    }
    public HostVo getHost() {
        return host;
    }
    public void setHost(HostVo host) {
        this.host = host;
    }
    public ResponseVo getResponse() {
        return response;
    }
    public void setResponse(ResponseVo response) {
        this.response = response;
    }
    public DestinationVo getDestination() {return destination;}
    public void setDestination(DestinationVo destination) {this.destination = destination;}

    public UserVo getUser() {
        return user;
    }
    public void setUser(UserVo user) {
        this.user = user;
    }
    public DeviceVo getDevice() {
        return device;
    }
    public void setDevice(DeviceVo device) {
        this.device = device;
    }
    public SecureVo getSecurity() {
        return security;
    }
    public void setSecurity(SecureVo security) {
        this.security = security;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class CallerVo {
        private String channel;
        private String channelIp;

        public String getChannel() {
            return channel;
        }
        public void setChannel(String channel) {
            this.channel = channel;
        }
        public String getChannelIp() {
            return channelIp;
        }
        public void setChannelIp(String channelIp) {
            this.channelIp = channelIp;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class HostVo {
        private String name;
        private String ip;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getIp() {
            return ip;
        }
        public void setIp(String ip) {
            this.ip = ip;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class ResponseVo {
        private String type;
        private String code;
        private String desc;
        private String duration;

        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
        public String getDesc() {
            return desc;
        }
        public void setDesc(String desc) {
            this.desc = desc;
        }
        public String getDuration() {
            return duration;
        }
        public void setDuration(String duration) {
            this.duration = duration;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class UserVo {
        private String id;
        private String ip;
        private String type;

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getIp() {
            return ip;
        }
        public void setIp(String ip) {
            this.ip = ip;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class DeviceVo {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class DestinationVo {
        private String name;
        private String ip;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getIp() {
            return ip;
        }
        public void setIp(String ip) {
            this.ip = ip;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class SecureVo {
        private String type;
        private String event;
        private String target;
        private String multiTarget;
        private String reason;
        private String exported;
        private String detail;
        //private Object detail;

        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getEvent() {
            return event;
        }
        public void setEvent(String event) {
            this.event = event;
        }
        public String getTarget() {
            return target;
        }
        public void setTarget(String target) {
            this.target = target;
        }
        public String getMultiTarget() {
            return multiTarget;
        }
        public void setMultiTarget(String multiTarget) {
            this.multiTarget = multiTarget;
        }
        public String getDetail() {
            return detail;
        }
        public void setDetail(String detail) {this.detail = detail;}
        public String getReason() {
            return reason;
        }
        public void setReason(String reason) {
            this.reason = reason;
        }
        public String getExported() {
            return exported;
        }
        public void setExported(String exported) {
            this.exported = exported;
        }
    }
    public String toString() {
        return "LampVO [timestamp=" + timestamp + ", service=" + service + ", operation=" + operation + ", bizTransactionId=" + bizTransactionId + ", transactionId=" + transactionId + ", logType=" + logType + ", payload=" + payload + ", caller=" + caller + ", host=" + host + ", response=" + response + ", user=" + user + ", device=" + device + ", security=" + security + ", destination=" + destination + "]";
    }
}
