package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.example.deliver_system.dto.UserProfileDTO;
import org.example.deliver_system.entity.AddressBook;
import org.example.deliver_system.entity.MerchantDetails;
import org.example.deliver_system.entity.ReviewHistory;
import org.example.deliver_system.entity.User;
import org.example.deliver_system.mapper.MerchantDetailsMapper;
import org.example.deliver_system.mapper.ReviewHistoryMapper;
import org.example.deliver_system.mapper.UserMapper;
import org.example.deliver_system.service.AddressBookService;
import org.example.deliver_system.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantDetailsMapper merchantDetailsMapper;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewHistoryMapper reviewHistoryMapper;

    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/list/merchant")
    public Result<List<UserProfileDTO>> listApprovedMerchants() {
        return Result.success(userMapper.findApprovedMerchants());
    }

    @PutMapping("/submit-review")
    public Result<String> submitReview(@RequestBody MerchantDetails reviewInfo, @RequestAttribute("userId") Long userId) {
        reviewInfo.setUserId(userId);
        reviewInfo.setStatus(1); // 将状态更新为“待审核”
        reviewInfo.setSubmitTime(java.time.LocalDateTime.now()); // 设置提交时间
        merchantDetailsMapper.updateReviewInfo(reviewInfo);
        reviewService.addHistory(userId, userId, "SUBMIT", "商家提交审核材料");
        return Result.success("审核已提交");
    }

    @GetMapping("/review/history")
    public Result<List<ReviewHistory>> getReviewHistory(@RequestAttribute("userId") Long userId) {
        return Result.success(reviewHistoryMapper.findByMerchantId(userId));
    }

    @GetMapping("/merchant/{id}")
    public Result<Map<String, Object>> getMerchantPublicInfo(@PathVariable Long id) {
        User user = userMapper.getById(id);
        if (user == null || !"MERCHANT".equals(user.getRole())) {
            return Result.error("商家不存在");
        }

        MerchantDetails details = merchantDetailsMapper.findByUserId(id);
        if (details == null || details.getStatus() != 2) {
            return Result.error("该商家暂未开业");
        }

        Map<String, Object> merchantData = new HashMap<>();
        merchantData.put("username", user.getUsername());
        merchantData.put("avatar", user.getAvatar());

        List<AddressBook> addresses = addressBookService.findByUserId(id);
        if (addresses != null && !addresses.isEmpty()) {
            AddressBook primaryAddress = addresses.get(0);
            merchantData.put("address", primaryAddress.getAddress());
            merchantData.put("contactName", primaryAddress.getContactName());
            merchantData.put("contactPhone", primaryAddress.getContactPhone());
        }

        return Result.success(merchantData);
    }
}
