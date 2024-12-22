package com.oneDev.healthcarebooking.service;

import com.oneDev.healthcarebooking.model.UserInfo;

public interface JwtService {
    String generateToken(UserInfo userInfo);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
}
