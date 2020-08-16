package com.cmall.searc.service;

import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class JestClientConfig {
    @Bean
    public io.searchbox.client.JestClient getJestClient(){
        JestClientFactory factory=new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://192.168.66.10:9200")
        .multiThreaded(true)
        .build());
        return  factory.getObject();
    }
}
