package com.fertigApp.backend.auth.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.http.HttpMethod.*;

//Habilitación de CORS para los navegadores
@Configuration
public class CORSConfiguration
{

    @Bean
    public CorsFilter corsFilter( ){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource( );
        CorsConfiguration config = new CorsConfiguration( );
        config.addAllowedOrigin( "*" );
        config.addAllowedHeader( "*" );
        config.addAllowedMethod( GET );
        config.addAllowedMethod( POST );
        config.addAllowedMethod( PUT );
        config.addAllowedMethod( PATCH );
        config.addAllowedMethod( DELETE );
        config.addAllowedMethod( OPTIONS );
        source.registerCorsConfiguration( "/**", config );
        return new CorsFilter(source);
    }

}
