package com.abmsa.controller;

import com.abmsa.entity.User;
import com.abmsa.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

/**
 * Shared base controller providing common helpers for all REST controllers.
 */
@RequiredArgsConstructor
public abstract class BaseController {

    protected final UserMapper userMapper;

    /**
     * Resolves the database user ID from the currently authenticated principal.
     * Returns {@code null} when authentication is absent.
     */
    protected Long resolveUserId(Authentication auth) {
        if (auth == null) return null;
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
        return user != null ? user.getId() : null;
    }
}
