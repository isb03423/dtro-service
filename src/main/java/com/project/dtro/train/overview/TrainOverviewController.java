package com.project.dtro.train.overview;

import com.project.dtro.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/train-overview")
@PreAuthorize("hasAnyRole('TRAIN_MANAGER', 'ADMIN')")
@RequiredArgsConstructor
public class TrainOverviewController {
    private final TrainOverviewService service;

    @GetMapping
    public ApiResponse<TrainOverviewResponse> getOverview() {
        return ApiResponse.ok(service.getOverview());
    }
}
