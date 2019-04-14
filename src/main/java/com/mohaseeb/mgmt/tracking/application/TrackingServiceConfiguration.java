package com.mohaseeb.mgmt.tracking.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrackingServiceConfiguration {

    @Bean
    public static TrackingService create(){
        return new InMemoryTrackingService();
    }
}
