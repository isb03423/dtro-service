package com.project.dtro.customerservice;

import com.project.dtro.qna.controller.QnaController;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import com.project.dtro.qna.dto.QnaRequest;
import com.project.dtro.qna.dto.QnaResponse;
import com.project.dtro.qna.service.QnaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QnaController.class)
@Import(CustomerServiceOverviewControllerTest.SecurityConfiguration.class)
class QnaCreatePermissionTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private QnaService service;
    private static final String BODY = """
            {"title":"질문", "type":"OTHER", "station":"문양", "content":"질문 내용입니다."}
            """;

    private void expectCreateAllowed() throws Exception {
        when(service.create(any(QnaRequest.class))).thenReturn(new QnaResponse(
                1L, "질문", "내용", "문양", QnaType.OTHER, QnaStatus.PENDING, null, null, null, null));
        mvc.perform(post("/api/qnas").with(csrf()).contentType("application/json").content(BODY))
                .andExpect(status().isCreated());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCannotCreate() throws Exception {
        mvc.perform(post("/api/qnas").with(csrf()).contentType("application/json").content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test @WithMockUser(roles = {"USER", "ADMIN"})
    void adminWithUserRoleCannotCreate() throws Exception {
        mvc.perform(post("/api/qnas").with(csrf()).contentType("application/json").content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test @WithMockUser(roles = "USER")
    void userCanCreate() throws Exception { expectCreateAllowed(); }

    @Test @WithMockUser(roles = "TRAIN_MANAGER")
    void trainManagerCanCreate() throws Exception { expectCreateAllowed(); }

    @Test
    void anonymousCannotCreate() throws Exception {
        mvc.perform(post("/api/qnas").with(csrf()).contentType("application/json").content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanStillAnswer() throws Exception {
        mvc.perform(patch("/api/qnas/1").with(csrf()).contentType("application/json")
                        .content("{\"replyContent\":\"관리자 답변 내용입니다.\"}"))
                .andExpect(status().isOk());
    }
}
