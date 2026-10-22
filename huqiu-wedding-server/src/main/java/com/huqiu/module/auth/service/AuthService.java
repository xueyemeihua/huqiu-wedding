package com.huqiu.module.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huqiu.common.context.LoginUser;
import com.huqiu.common.exception.BusinessException;
import com.huqiu.common.util.JwtUtil;
import com.huqiu.module.auth.dto.LoginDTO;
import com.huqiu.module.auth.dto.LoginVO;
import com.huqiu.module.user.entity.SysUser;
import com.huqiu.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;

    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        // MD5 简单演示：实际生产建议 BCrypt
        String md5 = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes());
        if (!md5.equalsIgnoreCase(user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        LoginUser loginUser = new LoginUser(user.getId(), user.getMerchantId(),
                user.getUsername(), user.getRealName(), user.getRole());
        String token = jwtUtil.generate(loginUser);
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .merchantId(user.getMerchantId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .build();
    }
}