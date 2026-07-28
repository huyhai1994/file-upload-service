package org.mini_lab.file_upload_service.configuration.security;

import org.mini_lab.file_upload_service.component.security.CustomAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SercurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   CustomAuthenticationProvider customAuthenticationProvider
    ) {
        httpSecurity
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/auth/register")
                )
                .authenticationProvider(customAuthenticationProvider)
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .anyRequest().authenticated()
                );

        return httpSecurity.build();
    }
}
