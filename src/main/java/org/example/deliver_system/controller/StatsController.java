package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.example.deliver_system.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/merchant")
    public Result<Map<String, Object>> getMerchantStats(@RequestAttribute("userId") Long merchantId) {
        Map<String, Object> stats = statsService.getMerchantDashboardStats(merchantId);
        return Result.success(stats);
    }
}
