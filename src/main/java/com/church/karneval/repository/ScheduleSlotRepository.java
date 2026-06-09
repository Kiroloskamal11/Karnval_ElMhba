package com.church.karneval.repository;

import com.church.karneval.model.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, UUID> {
    List<ScheduleSlot> findByTeamIdOrderBySlotOrder(UUID teamId);
    List<ScheduleSlot> findByStationId(UUID stationId);
    Optional<ScheduleSlot> findByTeamIdAndSlotOrder(UUID teamId, Integer slotOrder);
}
