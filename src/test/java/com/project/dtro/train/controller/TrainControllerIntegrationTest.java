package com.project.dtro.train.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TrainControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    // 로그인하지 않은 사용자의 열차 검색을 거부하는지 확인한다.
    void givenAnonymous_whenSearch_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/trains/search").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    // 일반 사용자에게 열차 검색 권한이 없는지 확인한다.
    void givenUserRole_whenSearch_thenForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/trains/search").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TRAIN_MANAGER")
    // 조건이 없어도 열차 목록이 페이지 형식으로 반환되는지 확인한다.
    void givenNoFilters_whenSearch_thenReturnPage() throws Exception {
        mockMvc.perform(post("/api/v1/trains/search").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(roles = "TRAIN_MANAGER")
    // 목록 주소의 GET 요청이 허용되지 않는지 확인한다.
    void givenManager_whenGetTrainList_thenMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/trains"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(roles = "TRAIN_MANAGER")
    // 열차 등록 API가 생성된 열차번호를 반환하는지 확인한다.
    void givenValidTrain_whenCreate_thenReturnGeneratedNumber() throws Exception {
        String body = """
                {
                  "formationName": "101편성",
                  "lineName": "1호선",
                  "carType": "전동차",
                  "carCount": 6,
                  "status": "AVAILABLE",
                  "introducedOn": "2024-01-15",
                  "lastInspectedOn": "2026-10-02"
                }
                """;

        mockMvc.perform(post("/api/v1/trains").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.trainNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.formationName").value("101편성"));
    }

    @Test
    @WithMockUser(roles = "TRAIN_MANAGER")
    // 같은 호선은 순번이 이어지고 다른 호선은 별도로 증가하는지 확인한다.
    void givenMultipleLines_whenCreateAndChangeLine_thenUseIndependentSequences() throws Exception {
        String body = """
                {
                  "formationName": "101편성",
                  "lineName": "1호선",
                  "carType": "전동차",
                  "carCount": 6,
                  "status": "AVAILABLE",
                  "introducedOn": "2024-01-15",
                  "lastInspectedOn": "2026-10-02"
                }
                """;
        String first = mockMvc.perform(post("/api/v1/trains").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String second = mockMvc.perform(post("/api/v1/trains").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String lineTwoBody = body.replace("1호선", "2호선");
        String lineTwo = mockMvc.perform(post("/api/v1/trains").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(lineTwoBody))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String lineThree = mockMvc.perform(post("/api/v1/trains").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body.replace("1호선", "3호선")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        String firstNumber = JsonPath.read(first, "$.data.trainNumber");
        String secondNumber = JsonPath.read(second, "$.data.trainNumber");
        String lineTwoNumber = JsonPath.read(lineTwo, "$.data.trainNumber");
        String lineThreeNumber = JsonPath.read(lineThree, "$.data.trainNumber");
        assertTrue(firstNumber.matches("TR-1[0-9]{3,}"));
        assertEquals(Long.parseLong(firstNumber.substring(4)) + 1,
                Long.parseLong(secondNumber.substring(4)));
        assertTrue(lineTwoNumber.matches("TR-2[0-9]{3,}"));
        assertTrue(lineThreeNumber.matches("TR-3[0-9]{3,}"));

        long firstId = ((Number) JsonPath.read(first, "$.data.id")).longValue();
        String moved = mockMvc.perform(put("/api/v1/trains/{id}", firstId).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(lineTwoBody))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String movedNumber = JsonPath.read(moved, "$.data.trainNumber");
        assertEquals(Long.parseLong(lineTwoNumber.substring(4)) + 1,
                Long.parseLong(movedNumber.substring(4)));
    }

    @Test
    @WithMockUser(roles = "TRAIN_MANAGER")
    // 열차의 검색·수정·삭제 흐름이 실제 저장소에 반영되는지 확인한다.
    void givenTrain_whenSearchUpdateAndDelete_thenPersistChanges() throws Exception {
        String body = """
                {
                  "formationName": "101편성",
                  "lineName": "1호선",
                  "carType": "전동차",
                  "carCount": 6,
                  "status": "AVAILABLE",
                  "introducedOn": "2024-01-15",
                  "lastInspectedOn": "2026-10-02"
                }
                """;
        String created = mockMvc.perform(post("/api/v1/trains").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(created, "$.data.id")).longValue();

        mockMvc.perform(post("/api/v1/trains/search").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"101", "lineName":"1호선", "status":"AVAILABLE", "page":0, "size":10, "sort":"id,asc"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(id));

        mockMvc.perform(post("/api/v1/trains/search").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"101", "lineName":"2호선", "status":"AVAILABLE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        mockMvc.perform(put("/api/v1/trains/{id}", id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("AVAILABLE", "RUNNING")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RUNNING"));

        mockMvc.perform(delete("/api/v1/trains/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/trains/{id}", id))
                .andExpect(status().isNotFound());
    }
}
