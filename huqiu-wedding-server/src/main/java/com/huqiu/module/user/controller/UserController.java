package com.huqiu.module.user.controller;

import com.huqiu.common.annotation.RequireRole;
import com.huqiu.common.result.PageResult;
import com.huqiu.common.result.Result;
import com.huqiu.module.user.dto.UserSaveDTO;
import com.huqiu.module.user.entity.SysUser;
import com.huqiu.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@RequireRole(1)  // 仅商家管理员
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public Result<PageResult<SysUser>> list(@RequestParam(defaultValue = "1") int current,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer role) {
        return Result.success(userService.page(current, size, keyword, role));
    }

    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody UserSaveDTO dto) {
        userService.save(dto);
        return Result.success();
    }

    @PutMapping("/toggle/{id}")
    public Result<Void> toggle(@PathVariable Long id) {
        userService.toggleStatus(id);
        return Result.success();
    }
}