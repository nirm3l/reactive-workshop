package com.example.demo.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("wiki")
public class WikiProperties {
    private boolean loadContent = true;

    private String url;

    public boolean isLoadContent() {
        return loadContent;
    }

    public void setLoadContent(boolean loadContent) {
        this.loadContent = loadContent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
