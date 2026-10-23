package com.project.dtro.trainoperation.controller;

import com.project.dtro.trainoperation.service.TrainOperationService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TrainOperationTimeValidationTest {

    @ParameterizedTest
    @CsvSource({
            "null, 09:00, 08:59, 400",
            "1, 09:00, 08:59, 400",
            "null, 09:00, 09:00, 200",
            "1, 09:00, 09:00, 200",
            "null, 09:00, 09:01, 200",
            "1, 09:00, 09:01, 200",
            "null, 23:59, 00:00, 400"
    })
    void validatesTimeOrderForCreateAndUpdate(String id, String departure, String arrival, int expectedStatus)
            throws Exception {
        TrainOperationService service = mock(TrainOperationService.class);
        var mvc = standaloneSetup(new TrainOperationController(service))
                .setControllerAdvice(new TrainOperationExceptionHandler())
                .build();

        mvc.perform(put("/api/train-operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rows":[{
                                  "id":%s,"trainId":1,"lineCode":"1",
                                  "departureStationCode":"A","arrivalStationCode":"B",
                                  "firstTrainTime":"%s","lastTrainTime":"%s"
                                }]}
                                """.formatted(id, departure, arrival)))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 400) {
            verifyNoInteractions(service);
        } else {
            verify(service).save(any());
        }
    }
}
