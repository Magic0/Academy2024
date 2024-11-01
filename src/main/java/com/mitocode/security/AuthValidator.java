package com.mitocode.security;

import org.springframework.stereotype.Component;

@Component
public class AuthValidator {

    public boolean hasAccess(){
        return true;
    }
}
