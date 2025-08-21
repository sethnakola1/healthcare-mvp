package com.healthcare.mvp.shared.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;



@Data
@AllArgsConstructor
public class AuthenticationDetails {
    private String userId;
    private String email;
    private String hospitalId;
    private List<String> roles;
}