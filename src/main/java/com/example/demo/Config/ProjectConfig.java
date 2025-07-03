package com.example.demo.Config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class ProjectConfig {
    
    @Bean
    public Cloudinary getCloudinary(){

        Map config = new HashMap<>();
        config.put("cloud_name", "dzyhoeurm");
        config.put("api_key", "826648439174773");
        config.put("api_secret", "wI6oL1bHuwTtDgaq3XBKZuZVpTQ");
        config.put("secure", true);
        return new Cloudinary(config);
    }
}
