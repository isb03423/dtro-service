package com.project.dtro.station.repository;

import com.project.dtro.station.domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {

    List<Station> findByLineCodeOrderByStationOrderAsc(String lineCode);

    List<Station> findByLineCodeOrderByStationOrderDesc(String lineCode);

    java.util.Optional<Station> findByStationCode(String stationCode);

    @Query("select distinct s.lineCode from Station s order by s.lineCode")
    List<String> findLineCodes();

}
