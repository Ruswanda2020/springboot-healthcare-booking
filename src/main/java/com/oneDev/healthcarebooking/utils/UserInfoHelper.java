package com.oneDev.healthcarebooking.utils;

import com.oneDev.healthcarebooking.model.UserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserInfoHelper {
    public UserInfo getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserInfo) authentication.getPrincipal();
    }
}
