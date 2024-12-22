package com.oneDev.healthcarebooking.service;

import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.request.AuthRequest;

public interface AuthService {

    UserInfo authenticate(AuthRequest authRequest);
}
