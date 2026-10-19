package com.project.dtro.qna.service;

import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import com.project.dtro.qna.dto.QnaRequest;
import com.project.dtro.qna.repository.QnaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QnaUpdateTest {
    @Mock
    private QnaRepository qnaRepository;

    @InjectMocks
    private QnaService service;

    @Test
    void givenAnsweredRecord_whenUpdate_thenPreserveReplyAndStatus() {
        Qna record = new Qna("제목", "내용", "반월당", QnaType.FACILITY);
        record.qnaAnswered("기존 답변을 유지합니다.");
        when(qnaRepository.findById(1L)).thenReturn(Optional.of(record));
        QnaRequest request = new QnaRequest();
        request.setTitle("  수정 제목  ");
        request.setContent("  <p>수정 내용</p>  ");
        request.setStation("  중앙로  ");
        request.setType(QnaType.SAFETY);

        var result = service.update(1L, request);

        assertEquals("수정 제목", result.getTitle());
        assertEquals("<p>수정 내용</p>", result.getContent());
        assertEquals("중앙로", result.getStation());
        assertEquals(QnaType.SAFETY, result.getType());
        assertEquals(QnaStatus.ANSWERED, result.getStatus());
        assertEquals("기존 답변을 유지합니다.", result.getReplyContent());
        verify(qnaRepository).flush();
    }

    @Test
    void givenMissingRecord_whenUpdate_thenReject() {
        when(qnaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.update(99L, new QnaRequest()));
    }
}
