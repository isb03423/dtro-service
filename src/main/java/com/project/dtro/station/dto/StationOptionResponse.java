package com.project.dtro.station.dto;

import com.project.dtro.station.domain.Station;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StationOptionResponse {
    private final String stationCode;
    private final String stationName;
    private final int stationOrder;

    // 역 엔티티를 선택 팝업에서 사용할 응답 데이터로 변환한다.
    public static StationOptionResponse from(Station station) {
        return new StationOptionResponse(
                station.getStationCode(),
                station.getStationName(),
                station.getStationOrder()
        );
    }
}
