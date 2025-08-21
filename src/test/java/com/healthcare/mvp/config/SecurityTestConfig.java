//package com.healthcare.mvp.config;
//
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Primary;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//
//
//@TestConfiguration
//public class SecurityTestConfig {
//
//    @Bean
//    @Primary
//    public UserDetailsService testUserDetailsService() {
//        return username -> {
//            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
//
//            // Mock different user roles for testing
//            switch (username) {
//                case "admin@hospital.com":
//                    authorities.add(new SimpleGrantedAuthority("ROLE_HOSPITAL_ADMIN"));
//                    break;
//                case "doctor@hospital.com":
//                    authorities.add(new SimpleGrantedAuthority("ROLE_DOCTOR"));
//                    break;
//                case "nurse@hospital.com":
//                    authorities.add(new SimpleGrantedAuthority("ROLE_NURSE"));
//                    break;
//                case "receptionist@hospital.com":
//                    authorities.add(new SimpleGrantedAuthority("ROLE_RECEPTIONIST"));
//                    break;
//                default:
//                    throw new UsernameNotFoundException("User not found: " + username);
//            }
//
//            return new User(username, "password", authorities);
//        };
//    }
//}