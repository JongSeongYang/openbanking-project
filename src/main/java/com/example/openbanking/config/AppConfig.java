package com.example.openbanking.config;

import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTokenizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private final ModelMapper strictModelMapper = new ModelMapper();
    private final ModelMapper camelModelMapper = new ModelMapper();

    @Bean
    public ModelMapper strictModelMapper() {
        strictModelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return strictModelMapper;
    }

    @Bean
    public ModelMapper toCamelCaseMapper() {
        camelModelMapper.getConfiguration()
                .setDestinationNameTokenizer(NameTokenizers.UNDERSCORE)
                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE);;
        return camelModelMapper;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public OkHttpClient okHttpClient() { return new OkHttpClient();}
}
