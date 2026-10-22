package com.huqiu.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huqiu.common.context.UserContext;
import com.huqiu.common.exception.BusinessException;
import com.huqiu.common.result.PageResult;
import com.huqiu.module.user.dto.UserSaveDTO;
import com.huqiu.module.user.entity.SysUser;
import com.huqiu.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;

    public PageResult<SysUser> page(int current, int size, String keyword, Integer role) {
        Long merchantId = UserContext.require().getMerchantId();
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getMerchantId, merchantId)
                .eq(role != null, SysUser::getRole, role)
                .like(StringUtils.hasText(keyword), SysUser::getRealName, keyword)
                .orderByDesc(SysUser::getId);
        Page<SysUser> page = userMapper.selectPage(new Page<>(current, size), qw);
        // 不返回密码
        page.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(UserSaveDTO dto) {
        Long merchantId = UserContext.require().getMerchantId();
        // 账号唯一校验
        SysUser exist = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (exist != null && !exist.getId().equals(dto.getId())) {
            throw new BusinessException("账号已存在");
        }
        if (dto.getId() == null) {
            if (!StringUtils.hasText(dto.getPassword())) {
                throw new BusinessException("新增用户密码不能为空");
            }
            SysUser user = new SysUser();
            user.setMerchantId(merchantId);
            user.setUsername(dto.getUsername());
            user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
            user.setRealName(dto.getRealName());
            user.setPhone(dto.getPhone());
            user.setRole(dto.getRole());
            user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
            userMapper.insert(user);
        } else {
            SysUser user = userMapper.selectById(dto.getId());
            if (user == null || !user.getMerchantId().equals(merchantId)) {
                throw new BusinessException("用户不存在");
            }
            user.setUsername(dto.getUsername());
            user.setRealName(dto.getRealName());
            user.setPhone(dto.getPhone());
            user.setRole(dto.getRole());
            if (dto.getStatus() != null) user.setStatus(dto.getStatus());
            if (StringUtils.hasText(dto.getPassword())) {
                user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
            }
            userMapper.updateById(user);
        }
    }

    public void toggleStatus(Long id) {
        Long merchantId = UserContext.require().getMerchantId();
        SysUser user = userMapper.selectById(id);
        if (user == null || !user.getMerchantId().equals(merchantId)) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(user.getStatus() != null && user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
    }
}