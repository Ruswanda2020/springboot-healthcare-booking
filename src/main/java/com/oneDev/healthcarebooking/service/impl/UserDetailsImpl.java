package com.oneDev.healthcarebooking.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oneDev.healthcarebooking.entity.Role;
import com.oneDev.healthcarebooking.entity.User;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.repository.RoleRepository;
import com.oneDev.healthcarebooking.repository.UserRepository;
import com.oneDev.healthcarebooking.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class UserDetailsImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CacheService cacheService;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        String USER_CACHE_KEY = "cache:user:";
        String USER_ROLES_CACHE_KEY = "cache:user:roles:";

        String userCacheKey = USER_CACHE_KEY + usernameOrEmail;
        String rolesCacheKey = USER_ROLES_CACHE_KEY + usernameOrEmail;

        Optional<User> userOpt = cacheService.get(userCacheKey, User.class);
        Optional<List<Role>> rolesOpt = cacheService.get(rolesCacheKey, new TypeReference<List<Role>>(){
        });

        if (userOpt.isPresent() && rolesOpt.isPresent()) {
            return UserInfo.builder()
                    .user(userOpt.get())
                    .roles(rolesOpt.get())
                    .build();
        }

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND, "User not found with: " + usernameOrEmail
                ));
        List<Role> roles = roleRepository.findByUserId(user.getUserId());

        UserInfo userInfo = UserInfo.builder()
                .user(user)
                .roles(roles)
                .build();

        cacheService.put(userCacheKey, user);
        cacheService.put(rolesCacheKey, roles);

        return userInfo;
    }
}
