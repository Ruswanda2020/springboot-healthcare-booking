package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.Role;
import com.oneDev.healthcarebooking.entity.User;
import com.oneDev.healthcarebooking.entity.UserRole;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.request.UserRegisterRequest;
import com.oneDev.healthcarebooking.model.request.UserUpdateRequest;
import com.oneDev.healthcarebooking.model.response.UserResponse;
import com.oneDev.healthcarebooking.repository.RoleRepository;
import com.oneDev.healthcarebooking.repository.UserRepository;
import com.oneDev.healthcarebooking.repository.UserRoleRepository;
import com.oneDev.healthcarebooking.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override @Transactional
    public UserResponse register(UserRegisterRequest userRegisterRequest) {

        if(existsByUserName(userRegisterRequest.getUsername())) {
            throw new ApplicationException(ExceptionType.USERNAME_ALREADY_EXISTS);
        }
        if (existsByEmail(userRegisterRequest.getEmail())) {
            throw new ApplicationException(ExceptionType.EMAIL_ALREADY_EXISTS);
        }
        if (!userRegisterRequest.getPassword().equals(userRegisterRequest.getPasswordConfirmation())){
            throw new ApplicationException(ExceptionType.BAD_REQUEST,
                    ExceptionType.BAD_REQUEST.getFormattedMessage("Passwords do not match"));
        }

        String encodedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());

        User user = User.builder()
                .username(userRegisterRequest.getUsername())
                .email(userRegisterRequest.getEmail())
                .enabled(true)
                .password(encodedPassword)
                .build();

        userRepository.save(user);
        Role role = roleRepository.findByName(RoleType.PATIENT).orElseThrow(
                () -> new ApplicationException(ExceptionType.ROLE_NOT_FOUND)
        );
        UserRole userRoleRelation = UserRole.builder()
                .userRoleId(new UserRole.UserRoleId(user.getUserId(), role.getRoleId()))
                .build();

        userRoleRepository.save(userRoleRelation);
        return UserResponse.from(user, List.of(role));
    }

    @Override
    public UserResponse findById(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND,
                        ExceptionType.USER_NOT_FOUND.getFormattedMessage("With id: " + userId)));
        List<Role> userRoles = roleRepository.findByUserId(userId);
        return UserResponse.from(user, userRoles);
    }


    @Override
    public UserResponse findByUsernameOrEmail(String usernameOrEmail) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail).
                orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND,
                        "Not found with username / email: " + usernameOrEmail));
        List<Role> userRoles = roleRepository.findByUserId(user.getUserId());
        return UserResponse.from(user, userRoles);
    }

    @Override @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest) {

        User user = userRepository.findById(userId).
                orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND,
                        "User not found with id: " + userId));

        if (userUpdateRequest.getCurrentPassword() != null && userUpdateRequest.getNewPassword() != null) {
            if (!passwordEncoder.matches(userUpdateRequest.getCurrentPassword(), user.getPassword())) {
                throw new ApplicationException(ExceptionType.INVALID_PASSWORD);
            }

            String encodedPassword = passwordEncoder.encode(userUpdateRequest.getNewPassword());
            user.setPassword(encodedPassword);
        }

        if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().equals(user.getUsername())) {
            if(existsByUserName(userUpdateRequest.getUsername())) {
                throw new ApplicationException(ExceptionType.USERNAME_ALREADY_EXISTS);
            }
            user.setUsername(userUpdateRequest.getUsername());
        }

        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().equals(user.getEmail())) {
            if(existsByEmail(userUpdateRequest.getEmail())) {
                throw new ApplicationException(ExceptionType.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(userUpdateRequest.getEmail());
        }

        userRepository.save(user);
        List<Role> roles = roleRepository.findByUserId(user.getUserId());
        return UserResponse.from(user, roles);
    }

    @Override
    public boolean existsByUserName(String userName) {
        return userRepository.existsByUsername(userName);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND,
                       "User not found With id: " + userId));

        userRoleRepository.deleteByIdUserId(userId);
        userRepository.delete(user);

    }

    @Override @Transactional
    public UserResponse grantUserRole(Long userId, RoleType roleType) {
        // Cari user berdasarkan ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND,
                        "User not found with ID: " + userId));

        // Cari role berdasarkan nama
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new ApplicationException(ExceptionType.ROLE_NOT_FOUND,
                        "Role not found with name: " + roleType));

        // Cek apakah user sudah memiliki role tersebut
        Optional<UserRole> existingUserRole = userRoleRepository.existByUserIdAndRoleId(userId, role.getRoleId());
        if (existingUserRole.isPresent()) {
            throw new IllegalArgumentException("User with ID: " + userId + " already has the role with role ID: " + role.getRoleId());
        }

        // Buat ID untuk UserRole
        UserRole.UserRoleId userRoleId = new UserRole.UserRoleId();
        userRoleId.setUserId(userId);
        userRoleId.setRoleId(role.getRoleId());

        // Buat UserRole baru
        UserRole newUserRole = UserRole.builder()
                .userRoleId(userRoleId)
                .build();

        // Simpan UserRole baru ke repository
        userRoleRepository.save(newUserRole);

        // Ambil semua role yang dimiliki oleh user
        List<Role> userRoles = roleRepository.findByUserId(userId);

        // Kembalikan response user dengan roles-nya
        return UserResponse.from(user, userRoles);
    }
}
