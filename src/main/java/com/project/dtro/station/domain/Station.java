package com.project.dtro.station.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "station")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationId;

    @Column(nullable = false, length = 10)
    private String lineCode;

    @Column(name = "station_code", nullable = false, unique = true, length = 10)
    private String stationCode;

    @Column(nullable = false, length = 50)
    private String stationName;

    @Column(nullable = false)
    private Integer stationOrder;
}
