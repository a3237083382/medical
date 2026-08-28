package com.ruoyi.business.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "medical.datasource")
public class MedicalDataSourceProperties
{
    private String defaultSource = "mock";
    private boolean fallbackEnabled = true;
    private Map<String, String> routes = new HashMap<>();
    private DigitalIndustry digitalIndustry = new DigitalIndustry();

    public String getDefaultSource() { return defaultSource; }
    public void setDefaultSource(String defaultSource) { this.defaultSource = defaultSource; }
    public boolean isFallbackEnabled() { return fallbackEnabled; }
    public void setFallbackEnabled(boolean fallbackEnabled) { this.fallbackEnabled = fallbackEnabled; }
    public Map<String, String> getRoutes() { return routes; }
    public void setRoutes(Map<String, String> routes) { this.routes = routes; }
    public DigitalIndustry getDigitalIndustry() { return digitalIndustry; }
    public void setDigitalIndustry(DigitalIndustry digitalIndustry) { this.digitalIndustry = digitalIndustry; }

    public static class DigitalIndustry
    {
        private boolean enabled = false;
        private String queryUrl;
        private Integer timeoutMillis = 5000;
        private String sdkClasspath;
        private String environment = "test";
        private String partnerId;
        private String sysId;
        private String appSubId;
        private String appToken;
        private String msgSecret;
        private String signSecret;
        private String dataServiceId;
        private String accessKeyId;
        private String accessKeySecret;
        private String sm2PublicKey;
        private String apiId;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getQueryUrl() { return queryUrl; }
        public void setQueryUrl(String queryUrl) { this.queryUrl = queryUrl; }
        public Integer getTimeoutMillis() { return timeoutMillis; }
        public void setTimeoutMillis(Integer timeoutMillis) { this.timeoutMillis = timeoutMillis; }
        public String getSdkClasspath() { return sdkClasspath; }
        public void setSdkClasspath(String sdkClasspath) { this.sdkClasspath = sdkClasspath; }
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public String getPartnerId() { return partnerId; }
        public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
        public String getSysId() { return sysId; }
        public void setSysId(String sysId) { this.sysId = sysId; }
        public String getAppSubId() { return appSubId; }
        public void setAppSubId(String appSubId) { this.appSubId = appSubId; }
        public String getAppToken() { return appToken; }
        public void setAppToken(String appToken) { this.appToken = appToken; }
        public String getMsgSecret() { return msgSecret; }
        public void setMsgSecret(String msgSecret) { this.msgSecret = msgSecret; }
        public String getSignSecret() { return signSecret; }
        public void setSignSecret(String signSecret) { this.signSecret = signSecret; }
        public String getDataServiceId() { return dataServiceId; }
        public void setDataServiceId(String dataServiceId) { this.dataServiceId = dataServiceId; }
        public String getAccessKeyId() { return accessKeyId; }
        public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }
        public String getAccessKeySecret() { return accessKeySecret; }
        public void setAccessKeySecret(String accessKeySecret) { this.accessKeySecret = accessKeySecret; }
        public String getSm2PublicKey() { return sm2PublicKey; }
        public void setSm2PublicKey(String sm2PublicKey) { this.sm2PublicKey = sm2PublicKey; }
        public String getApiId() { return apiId; }
        public void setApiId(String apiId) { this.apiId = apiId; }
    }
}

