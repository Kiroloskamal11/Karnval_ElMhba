package com.church.karneval.repository;

import com.church.karneval.model.Arrival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArrivalRepository extends JpaRepository<Arrival, UUID> {
    Optional<Arrival> findByTeamIdAndStationId(UUID teamId, UUID stationId);
    List<Arrival> findByTeamId(UUID teamId);
    List<Arrival> findByStationId(UUID stationId);
}
