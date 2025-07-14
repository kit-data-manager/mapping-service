/*
 * Copyright 2022 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.mappingservice.configuration;

import edu.kit.datamanager.security.filter.KeycloakTokenFilter;
import edu.kit.datamanager.security.filter.NoAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author jejkal
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private Optional<KeycloakTokenFilter> keycloakTokenFilterBean;
    @Autowired
    private ApplicationProperties applicationProperties;

    private static final String[] AUTH_WHITELIST_SWAGGER_UI = {
        // -- Swagger UI v2
        "/v2/api-docs",
        "/swagger-resources",
        "/swagger-resources/**",
        "/configuration/ui",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",
        // -- Swagger UI v3 (OpenAPI)
        "/v3/api-docs/**",
        "/swagger-ui/**"
    // other public endpoints of your API may be appended to this array
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        List<AntPathRequestMatcher> securedEndpointMatchers;

        if (applicationProperties.isAuthEnabled()) {
            logger.trace("Authentication is ENABLED. Collecting secured endpoints.");
            securedEndpointMatchers = Arrays.asList(
                    new AntPathRequestMatcher("/api/v1/mappingAdministration/types/*/execute", "POST"),
                    new AntPathRequestMatcher("/api/v1/mappingAdministration/reloadTypes", "GET"),
                    new AntPathRequestMatcher("/api/v1/mappingAdministration", "PUT"),
                    new AntPathRequestMatcher("/api/v1/mappingAdministration", "POST")
            );
        } else {
            logger.trace("Authentication is DISABLED. Not securing endpoints.");
            securedEndpointMatchers = List.of();
        }

        HttpSecurity httpSecurity = http.authorizeHttpRequests(
                authorize -> authorize.
                        requestMatchers(HttpMethod.OPTIONS).permitAll().
                        requestMatchers(EndpointRequest.to(
                                InfoEndpoint.class,
                                HealthEndpoint.class
                        )).permitAll().
                        requestMatchers(EndpointRequest.toAnyEndpoint()).hasAnyRole("ANONYMOUS", "ADMINISTRATOR", "ACTUATOR", "SERVICE_WRITE").
                        requestMatchers(new AntPathRequestMatcher("/static/**")).permitAll().
                        requestMatchers(new AntPathRequestMatcher("/error")).permitAll().
                        requestMatchers(securedEndpointMatchers.toArray(AntPathRequestMatcher[]::new)).hasRole(applicationProperties.getMappingAdminRole()). //endpoint filters only active if auth is enabled
                        requestMatchers(AUTH_WHITELIST_SWAGGER_UI).permitAll().
                        anyRequest().authenticated()
        ).
                httpBasic(Customizer.withDefaults()).
                cors(cors -> cors.configurationSource(corsConfigurationSource())).
                sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        logger.info("CSRF disabled!");
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        if (keycloakTokenFilterBean.isPresent()) {
            logger.trace("Adding Keycloak filter to filter chain.");
            httpSecurity.addFilterAfter(keycloakTokenFilterBean.get(), BasicAuthenticationFilter.class);
        } else {
            logger.trace("Keycloak not configured. Skip adding keycloak filter to filter chain.");
        }

        if (!applicationProperties.isAuthEnabled()) {
            logger.info("Adding 'NoAuthenticationFilter' to filter chain.");
            AuthenticationManager defaultAuthenticationManager = http.getSharedObject(AuthenticationManager.class);
            httpSecurity = httpSecurity.addFilterAfter(new NoAuthenticationFilter("vkfvoswsohwrxgjaxipuiyyjgubggzdaqrcuupbugxtnalhiegkppdgjgwxsmvdb", defaultAuthenticationManager), BasicAuthenticationFilter.class);
        } else {
            logger.info("Skip adding NoAuthenticationFilter to filter chain.");
        }

        logger.trace("Turning off cache control.");
        httpSecurity.headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable));

        return httpSecurity.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern(applicationProperties.getAllowedOriginPattern());
        config.setAllowedHeaders(Arrays.asList(applicationProperties.getAllowedHeaders()));
        config.setAllowedMethods(Arrays.asList(applicationProperties.getAllowedMethods()));
        config.setExposedHeaders(Arrays.asList(applicationProperties.getExposedHeaders()));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
