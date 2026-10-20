package com.project.dtro.customerservice;

import com.project.dtro.customerservice.controller.CustomerServiceOptionsController;
import com.project.dtro.station.dto.StationOptionResponse;
import com.project.dtro.trainoperation.service.TrainOperationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerServiceOptionsController.class)
@Import(CustomerServiceOverviewControllerTest.SecurityConfiguration.class)
class CustomerServiceOptionsControllerTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private TrainOperationService service;

    @Test
    @WithMockUser(roles = "USER")
    void userCanLoadLines() throws Exception {
        when(service.lines()).thenReturn(List.of("1", "2"));
        mvc.perform(get("/api/customer-service-options/lines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanLoadOnlySelectedLineStations() throws Exception {
        when(service.stations("2", "DOWN")).thenReturn(List.of(new StationOptionResponse("201", "문양", 1)));
        mvc.perform(get("/api/customer-service-options/stations").param("lineCode", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].stationName").value("문양"));
        verify(service).stations("2", "DOWN");
    }

    @Test
    void anonymousCannotLoadOptions() throws Exception {
        mvc.perform(get("/api/customer-service-options/lines"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void unsupportedRoleCannotLoadOptions() throws Exception {
        mvc.perform(get("/api/customer-service-options/stations").param("lineCode", "1"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
