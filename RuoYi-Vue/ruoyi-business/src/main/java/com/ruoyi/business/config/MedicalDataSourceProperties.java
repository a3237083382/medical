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

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getQueryUrl() { return queryUrl; }
        public void setQueryUrl(String queryUrl) { this.queryUrl = queryUrl; }
        public Integer getTimeoutMillis() { return timeoutMillis; }
        public void setTimeoutMillis(Integer timeoutMillis) { this.timeoutMillis = timeoutMillis; }
    }
}

