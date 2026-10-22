package com.project.dtro.customerservice.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.customerservice.dto.CustomerServiceOverviewResponse;
import com.project.dtro.customerservice.service.CustomerServiceOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer-service-overview")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CustomerServiceOverviewController {
    private final CustomerServiceOverviewService service;

    @GetMapping
    public ApiResponse<CustomerServiceOverviewResponse> getOverview() {
        return ApiResponse.ok(service.getOverview());
    }
}
