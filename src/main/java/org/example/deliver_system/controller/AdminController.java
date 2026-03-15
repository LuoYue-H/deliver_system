package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.example.deliver_system.dto.UserProfileDTO;
import org.example.deliver_system.entity.User;
import org.example.deliver_system.mapper.MerchantDetailsMapper;
import org.example.deliver_system.mapper.UserMapper;
import org.example.deliver_system.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantDetailsMapper merchantDetailsMapper;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/review/list")
    public Result<List<UserProfileDTO>> listPendingMerchants() {
        return Result.success(userMapper.findMerchantsByStatus(1));
    }

    @PutMapping("/review/approve/{id}")
    public Result<String> approveMerchant(@PathVariable Long id, @RequestAttribute("userId") Long adminId) {
        merchantDetailsMapper.updateStatus(id, 2);
        reviewService.addHistory(id, adminId, "APPROVE", "审核通过");
        return Result.success("审核通过");
    }

    @PutMapping("/review/reject/{id}")
    public Result<String> rejectMerchant(@PathVariable Long id, @RequestBody Map<String, String> payload, @RequestAttribute("userId") Long adminId) {
        String reason = payload.get("reason");
        merchantDetailsMapper.updateStatusAndReason(id, 3, reason);
        reviewService.addHistory(id, adminId, "REJECT", reason);
        return Result.success("审核驳回");
    }

    @GetMapping("/users/list")
    public Result<List<UserProfileDTO>> listAllUsers() {
        return Result.success(userMapper.findAllUsersWithDetails());
    }

    @PutMapping("/trigger-reaudit/{id}")
    public Result<String> triggerReaudit(@PathVariable Long id, @RequestBody Map<String, String> payload, @RequestAttribute("userId") Long adminId) {
        String reason = payload.get("reason");
        if (reason == null || reason.isBlank()) {
            return Result.error("必须提供要求重新审核的原因");
        }
        merchantDetailsMapper.updateStatus(id, 0); // 设置为“未提交”状态
        reviewService.addHistory(id, adminId, "RE_AUDIT_REQUESTED", reason);
        return Result.success("已要求该商家重新提交审核");
    }

    @PostMapping("/add-rider")
    public Result<String> addRider(@RequestBody User rider) {
        // 这里应该添加密码加密逻辑
        userMapper.insert(rider);
        return Result.success("骑手添加成功");
    }

    @GetMapping("/riders")
    public Result<List<User>> listRiders() {
        return Result.success(userMapper.findUsersByRole("RIDER"));
    }

    @DeleteMapping("/rider/{id}")
    public Result<String> deleteRider(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/rider/{id}")
    public Result<String> updateRider(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        // 同样，密码应该加密处理
        userMapper.updateUser(user);
        return Result.success("更新成功");
    }
}
