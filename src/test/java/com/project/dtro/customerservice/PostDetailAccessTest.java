package com.project.dtro.customerservice;

import com.project.dtro.common.config.SpringConfig;
import com.project.dtro.complaint.controller.ComplaintController;
import com.project.dtro.complaint.domain.Complaint;
import com.project.dtro.complaint.domain.ComplaintType;
import com.project.dtro.complaint.repository.ComplaintRepository;
import com.project.dtro.complaint.service.ComplaintService;
import com.project.dtro.qna.controller.QnaController;
import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaType;
import com.project.dtro.qna.repository.QnaRepository;
import com.project.dtro.qna.service.QnaService;
import com.project.dtro.security.MemberPrincipal;
import com.project.dtro.user.domain.Member;
import com.project.dtro.user.domain.MemberRole;
import com.project.dtro.user.repository.MemberRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ComplaintController.class, QnaController.class})
@Import({SpringConfig.class, ComplaintService.class, QnaService.class})
class PostDetailAccessTest {
    @Autowired MockMvc mvc;
    @MockitoBean ComplaintRepository complaints;
    @MockitoBean QnaRepository qnas;
    @MockitoBean MemberRepository members;

    @ParameterizedTest
    @ValueSource(strings = {"complaints", "qnas"})
    void anonymousDetailReturns401(String resource) throws Exception {
        mvc.perform(get("/api/" + resource + "/1"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(complaints, qnas);
    }

    @ParameterizedTest
    @CsvSource({"complaints,10,USER,200", "complaints,11,USER,403",
            "complaints,11,ADMIN,200", "complaints,11,TRAIN_MANAGER,403",
            "qnas,10,USER,200", "qnas,11,USER,403", "qnas,11,ADMIN,200",
            "qnas,10,TRAIN_MANAGER,200", "qnas,11,TRAIN_MANAGER,403"})
    void realSecurityAndServiceEnforceDetailAccess(String resource, Long id, String role, int status) throws Exception {
        Member owner = Member.builder().id(10L).build();
        if (resource.equals("complaints")) {
            var record = new Complaint("title", "private body", "station", ComplaintType.FACILITY);
            ReflectionTestUtils.setField(record, "createdBy", owner);
            when(complaints.findById(1L)).thenReturn(Optional.of(record));
        } else {
            var record = new Qna("title", "private body", "station", QnaType.FACILITY);
            ReflectionTestUtils.setField(record, "createdBy", owner);
            when(qnas.findById(1L)).thenReturn(Optional.of(record));
        }
        var principal = new MemberPrincipal(id, "test", "unused", true,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        var member = Member.builder().id(id).userid("test").password("unused")
                .role(MemberRole.valueOf(role)).build();
        when(members.findById(id)).thenReturn(Optional.of(member));
        var result = mvc.perform(get("/api/" + resource + "/1").with(user(principal)))
                .andExpect(status().is(status));
        if (status == 200) result.andExpect(jsonPath("$.data.content").value("private body"));
        else result.andExpect(jsonPath("$.data.content").doesNotExist());
    }

    @ParameterizedTest
    @CsvSource({"question,10,USER,false,200", "question,10,USER,true,409",
            "question,11,ADMIN,false,403", "question,11,ADMIN,true,403",
            "question,11,USER,false,403", "answer,11,ADMIN,false,200",
            "answer,10,USER,false,403", "answer,11,ADMIN,true,409",
            "answer-edit,11,ADMIN,true,200", "answer-edit,11,ADMIN,false,409",
            "answer-edit,10,USER,true,403", "answer-edit,10,TRAIN_MANAGER,true,403"})
    void qnaWorkflowEnforcesRolesAndState(String action, Long id, String role, boolean answered, int status) throws Exception {
        var record = new Qna("original title", "original body", "station", QnaType.FACILITY);
        ReflectionTestUtils.setField(record, "createdBy", Member.builder().id(10L).build());
        if (answered) record.qnaAnswered("original answer");
        when(qnas.findById(1L)).thenReturn(Optional.of(record));
        var principal = new MemberPrincipal(id, "test", "unused", true,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        when(members.findById(id)).thenReturn(Optional.of(Member.builder().id(id).userid("test")
                .password("unused").role(MemberRole.valueOf(role)).build()));
        var request = action.equals("question")
                ? org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/qnas/1")
                : org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        action.equals("answer-edit") ? "/api/qnas/1/answer" : "/api/qnas/1");
        String body = action.equals("question")
                ? "{\"title\":\"new title\",\"content\":\"new content\",\"station\":\"new station\",\"type\":\"SAFETY\"}"
                : "{\"replyContent\":\"updated answer text\"}";
        var result = mvc.perform(request.with(user(principal))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json").content(body))
                .andExpect(status().is(status));
        if (!action.equals("question") || status != 200) {
            org.junit.jupiter.api.Assertions.assertEquals("original body", record.getContent());
            org.junit.jupiter.api.Assertions.assertEquals("original title", record.getTitle());
        }
        if (action.equals("answer-edit") && status == 200) {
            result.andExpect(jsonPath("$.data.replyContent").value("updated answer text"))
                    .andExpect(jsonPath("$.data.status").value("ANSWERED"));
        }
        if (status == 409) result.andExpect(jsonPath("$.code").value("QNA_INVALID_STATE"));
    }
}
