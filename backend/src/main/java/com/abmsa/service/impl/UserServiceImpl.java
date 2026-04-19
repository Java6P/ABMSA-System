package com.abmsa.service.impl;

import com.abmsa.common.Result;
import com.abmsa.config.JwtUtil;
import com.abmsa.entity.User;
import com.abmsa.mapper.UserMapper;
import com.abmsa.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Result<Map<String, Object>> login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Result.error(401, "User not found");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(401, "Invalid credentials");
        }
        String token = jwtUtil.generateToken(username);

        // Do not expose the password
        user.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        log.info("User '{}' logged in successfully", username);
        return Result.success(data);
    }

    @Override
    public Result<Void> register(String username, String password, String email) {
        long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            return Result.error(400, "Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        userMapper.insert(user);
        log.info("User '{}' registered successfully", username);
        return Result.success();
    }

    @Override
    public Result<User> getUserInfo(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Result.error(404, "User not found");
        }
        user.setPassword(null);
        return Result.success(user);
    }
}
