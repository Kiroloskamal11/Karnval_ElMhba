package com.church.karneval.service;

import com.church.karneval.dto.TeamLiveStatus;
import com.church.karneval.model.EventConfig;
import com.church.karneval.model.ScheduleSlot;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ScheduleService {
    Map<UUID, List<ScheduleSlot>> getFullSchedule();

    List<ScheduleSlot> getTeamSchedule(UUID teamId);

    ScheduleSlot updateSlot(UUID slotId, LocalTime startTime, LocalTime endTime);

    ScheduleSlot getCurrentSlot(UUID teamId);

    ScheduleSlot getNextSlot(UUID teamId);

    TeamLiveStatus getTeamLiveStatus(UUID teamId);

    TeamLiveStatus getTeamLiveStatus(UUID teamId, LocalTime currentTime);

    List<TeamLiveStatus> getAllTeamsLiveStatus();

    List<TeamLiveStatus> getAllTeamsLiveStatus(LocalTime currentTime);

    EventConfig getEventConfig();

    EventConfig updateEventConfig(EventConfig config, UUID adminId);
}
