package com.abmsa.service;

import com.abmsa.common.Result;
import com.abmsa.entity.User;

import java.util.Map;

public interface UserService {

    Result<Map<String, Object>> login(String username, String password);

    Result<Void> register(String username, String password, String email);

    Result<User> getUserInfo(String username);
}
