package com.cool.server.controller.client;

import com.cool.common.Result;
import com.cool.server.service.UserCheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 用户签到控制器
 */
@Tag(name = "用户签到", description = "用户签到相关接口")
@RestController
@RequestMapping("/checkin")
public class UserCheckinController {

    @Autowired
    private UserCheckinService userCheckinService;

    @Operation(summary = "检查用户今天是否已签到", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/{userId}/status")
    public Result<Boolean> hasCheckedInToday(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        boolean checkedIn = userCheckinService.hasCheckedInToday();
        return Result.success(checkedIn);
    }

    @Operation(summary = "用户签到", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("")
    public Result<Integer> checkin(){
        int pointsEarned = userCheckinService.checkin();
        return Result.success(pointsEarned);
    }

    @Operation(summary = "获取用户连续签到天数", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/consecutive/{userId}")
    public Result<Integer> getConsecutiveCheckinDays() {
        int days = userCheckinService.getConsecutiveCheckinDays();
        return Result.success(days);
    }

    @Operation(summary = "检查用户在指定日期是否签到", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/date/{userId}")
    public Result<Boolean> hasCheckedInOnDate(
            @Parameter(description = "日期，格式：YYYY-MM-DD") @RequestParam String date) {
        LocalDate checkinDate = LocalDate.parse(date);
        boolean checkedIn = userCheckinService.hasCheckedInOnDate(checkinDate);
        return Result.success(checkedIn);
    }
}
