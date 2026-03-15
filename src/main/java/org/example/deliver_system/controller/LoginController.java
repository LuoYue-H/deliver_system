package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.example.deliver_system.dto.UserProfileDTO;
import org.example.deliver_system.entity.MerchantDetails;
import org.example.deliver_system.entity.User;
import org.example.deliver_system.mapper.MerchantDetailsMapper;
import org.example.deliver_system.mapper.UserMapper;
import org.example.deliver_system.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantDetailsMapper merchantDetailsMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public Result<Map<String, String>> login(@RequestBody User user) {
        User dbUser = userMapper.getByUsername(user.getUsername());
        if (dbUser == null || !dbUser.getPassword().equals(user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUsername(), dbUser.getRole());
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("userId", dbUser.getId().toString());
        map.put("role", dbUser.getRole());
        return Result.success(map);
    }

    @PostMapping("/register")
    @Transactional
    public Result<String> register(@RequestBody User user) {
        User dbUser = userMapper.getByUsername(user.getUsername());
        if (dbUser != null) {
            return Result.error("用户名已存在");
        }
        userMapper.insert(user); // 这会通过 useGeneratedKeys 将ID设置回user对象

        if ("MERCHANT".equals(user.getRole())) {
            MerchantDetails details = new MerchantDetails();
            details.setUserId(user.getId());
            details.setStatus(0); // 初始状态为未提交
            merchantDetailsMapper.insert(details);
        }
        return Result.success("注册成功");
    }

    @GetMapping("/profile")
    public Result<UserProfileDTO> getProfile(@RequestAttribute("userId") Long userId) {
        User user = userMapper.getById(userId);
        user.setPassword(null);

        UserProfileDTO profileDTO = new UserProfileDTO();
        BeanUtils.copyProperties(user, profileDTO);

        if ("MERCHANT".equals(user.getRole())) {
            MerchantDetails details = merchantDetailsMapper.findByUserId(userId);
            profileDTO.setMerchantDetails(details);
        }
        return Result.success(profileDTO);
    }

    @PutMapping("/update-password")
    public Result<String> updatePassword(@RequestBody Map<String, String> params, @RequestAttribute("userId") Long userId) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        User user = userMapper.getById(userId);
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("旧密码错误");
        }

        user.setPassword(newPassword);
        userMapper.updateUser(user);
        return Result.success("密码修改成功");
    }

    @PutMapping("/update")
    @Transactional
    public Result<String> updateProfile(@RequestBody UserProfileDTO profileDTO, @RequestAttribute("userId") Long userId) {
        User user = userMapper.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新基本信息
        user.setUsername(profileDTO.getUsername());
        user.setPhone(profileDTO.getPhone());
        user.setAvatar(profileDTO.getAvatar());
        userMapper.updateProfile(user);

        // 如果是商家，更新经营品类
        if ("MERCHANT".equals(user.getRole()) && profileDTO.getMerchantDetails() != null) {
            merchantDetailsMapper.updateCategory(userId, profileDTO.getMerchantDetails().getCategory());
        }

        return Result.success("个人信息更新成功");
    }
}
