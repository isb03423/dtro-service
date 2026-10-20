package com.project.dtro.customerservice;

import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.customerservice.controller.CustomerServiceOverviewController;
import com.project.dtro.customerservice.dto.CustomerServiceOverviewResponse;
import com.project.dtro.customerservice.service.CustomerServiceOverviewService;
import com.project.dtro.qna.domain.QnaStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerServiceOverviewController.class)
@Import(CustomerServiceOverviewControllerTest.SecurityConfiguration.class)
class CustomerServiceOverviewControllerTest {
    private static final String URL = "/api/v1/customer-service-overview";

    @Autowired private MockMvc mvc;
    @MockitoBean private CustomerServiceOverviewService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenAdmin_whenGetOverview_thenReturnCommonResponse() throws Exception {
        when(service.getOverview()).thenReturn(new CustomerServiceOverviewResponse(0,
                Map.of(ComplaintStatus.PENDING, 0L, ComplaintStatus.ANSWERED, 0L), 0,
                Map.of(QnaStatus.PENDING, 0L, QnaStatus.ANSWERED, 0L), 0, 0, List.of(),
                Instant.parse("2026-10-19T06:00:00Z")));

        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingTotal").value(0))
                .andExpect(jsonPath("$.data.recentRequests").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void givenUser_whenGetOverview_thenForbidAccess() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "TRAIN_MANAGER")
    void givenTrainManager_whenGetOverview_thenForbidAccess() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void givenAnonymous_whenGetOverview_thenRequireAuthentication() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class SecurityConfiguration {
        @Bean
        SecurityFilterChain overviewSecurity(HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults()).build();
        }
    }
}
