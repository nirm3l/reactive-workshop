package com.example.demo.configuration;

import com.example.demo.properties.WikiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WikiClientConfiguration {

    @Bean
    public WebClient wikiClient(WikiProperties properties) {
        return WebClient
                .create(properties.getUrl());
    }
}
