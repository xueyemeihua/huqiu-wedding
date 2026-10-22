package com.huqiu.module.auth.controller;

import com.huqiu.common.context.UserContext;
import com.huqiu.common.result.Result;
import com.huqiu.module.auth.dto.LoginDTO;
import com.huqiu.module.auth.dto.LoginVO;
import com.huqiu.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @GetMapping("/me")
    public Result<Object> me() {
        return Result.success(UserContext.require());
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}