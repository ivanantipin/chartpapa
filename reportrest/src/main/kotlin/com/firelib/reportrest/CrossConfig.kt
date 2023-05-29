package com.firelib.reportrest

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {
    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.addAllowedOriginPattern("*") // Provide the allowed origins here
        config.addAllowedHeader("*") // Provide the allowed headers here
        config.addAllowedMethod("*") // Provide the allowed methods here
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}