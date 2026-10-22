package com.project.dtro.trainoperation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OperationSearchRequest {
    private Long trainId;
    private String lineCode;
    private String direction;
    private int page = 0;
    private int size = 20;
    private String sort = "id,desc";
}
