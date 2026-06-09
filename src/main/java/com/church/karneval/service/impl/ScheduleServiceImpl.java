package com.church.karneval.service.impl;

import com.church.karneval.dto.TeamLiveStatus;
import com.church.karneval.model.EventConfig;
import com.church.karneval.model.ScheduleSlot;
import com.church.karneval.model.Team;
import com.church.karneval.repository.EventConfigRepository;
import com.church.karneval.repository.ScheduleSlotRepository;
import com.church.karneval.repository.TeamRepository;
import com.church.karneval.repository.UserRepository;
import com.church.karneval.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleSlotRepository scheduleSlotRepository;
    private final TeamRepository teamRepository;
    private final EventConfigRepository eventConfigRepository;
    private final UserRepository userRepository;

    public ScheduleServiceImpl(
            ScheduleSlotRepository scheduleSlotRepository,
            TeamRepository teamRepository,
            EventConfigRepository eventConfigRepository,
            UserRepository userRepository) {
        this.scheduleSlotRepository = scheduleSlotRepository;
        this.teamRepository = teamRepository;
        this.eventConfigRepository = eventConfigRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, List<ScheduleSlot>> getFullSchedule() {
        return scheduleSlotRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        slot -> slot.getTeam().getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleSlot> getTeamSchedule(UUID teamId) {
        return scheduleSlotRepository
                .findByTeamIdOrderBySlotOrder(teamId);
    }

    @Override
    @Transactional
    public ScheduleSlot updateSlot(UUID slotId,
            LocalTime startTime, LocalTime endTime) {
        ScheduleSlot existing = scheduleSlotRepository
                .findById(slotId)
                .orElseThrow(() -> new RuntimeException(
                        "لم يتم العثور على الفترة الزمنية بالمعرف: "
                                + slotId));

        if (startTime != null) {
            existing.setStartTime(startTime);
        }
        if (endTime != null) {
            existing.setEndTime(endTime);
        }

        return scheduleSlotRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleSlot getCurrentSlot(UUID teamId) {
        LocalTime now = LocalTime.now();
        List<ScheduleSlot> slots = scheduleSlotRepository
                .findByTeamIdOrderBySlotOrder(teamId);
        for (ScheduleSlot slot : slots) {
            if (!now.isBefore(slot.getStartTime()) &&
                    now.isBefore(slot.getEndTime())) {
                return slot;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleSlot getNextSlot(UUID teamId) {
        LocalTime now = LocalTime.now();
        List<ScheduleSlot> slots = scheduleSlotRepository
                .findByTeamIdOrderBySlotOrder(teamId);
        for (ScheduleSlot slot : slots) {
            if (slot.getStartTime().isAfter(now)) {
                return slot;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public TeamLiveStatus getTeamLiveStatus(UUID teamId) {
        return getTeamLiveStatus(teamId, LocalTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public TeamLiveStatus getTeamLiveStatus(
            UUID teamId, LocalTime currentTime) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException(
                        "لم يتم العثور على الفريق بالمعرف: " + teamId));

        List<ScheduleSlot> slots = scheduleSlotRepository
                .findByTeamIdOrderBySlotOrder(teamId);

        EventConfig config = eventConfigRepository.findAll()
                .stream().findFirst().orElse(null);
        int alertMinutes = (config != null) ? config.getAlertMinutes() : 5;

        TeamLiveStatus status = new TeamLiveStatus();
        status.setTeamId(team.getId());
        status.setTeamName(team.getName());
        status.setTeamColor(team.getColor());
        status.setTeamColorHex(team.getColorHex());

        ScheduleSlot currentSlot = null;
        ScheduleSlot nextSlot = null;

        for (int i = 0; i < slots.size(); i++) {
            ScheduleSlot slot = slots.get(i);
            if (!currentTime.isBefore(slot.getStartTime()) &&
                    currentTime.isBefore(slot.getEndTime())) {
                currentSlot = slot;
                if (i + 1 < slots.size()) {
                    nextSlot = slots.get(i + 1);
                }
                break;
            }
        }

        if (currentSlot == null) {
            for (ScheduleSlot slot : slots) {
                if (slot.getStartTime().isAfter(currentTime)) {
                    nextSlot = slot;
                    break;
                }
            }
        }

        if (currentSlot != null) {
            status.setCurrentStationName(
                    currentSlot.getStation().getName());
            status.setCurrentStationLocation(
                    currentSlot.getStation().getLocationHint());

            long secondsRemaining = ChronoUnit.SECONDS
                    .between(currentTime, currentSlot.getEndTime());
            status.setTimeRemainingSeconds(secondsRemaining);
            status.setIsAlertTime(
                    secondsRemaining <= (long) alertMinutes * 60);
        } else {
            status.setCurrentStationName(null);
            status.setCurrentStationLocation(null);
            status.setTimeRemainingSeconds(null);
            status.setIsAlertTime(false);
        }

        if (nextSlot != null) {
            status.setNextStationName(
                    nextSlot.getStation().getName());
            status.setNextStationLocation(
                    nextSlot.getStation().getLocationHint());
        } else {
            status.setNextStationName(null);
            status.setNextStationLocation(null);
        }

        return status;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamLiveStatus> getAllTeamsLiveStatus() {
        return getAllTeamsLiveStatus(LocalTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamLiveStatus> getAllTeamsLiveStatus(
            LocalTime currentTime) {
        List<Team> teams = teamRepository.findAll();
        List<TeamLiveStatus> result = new ArrayList<>();
        for (Team team : teams) {
            result.add(getTeamLiveStatus(
                    team.getId(), currentTime));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventConfig getEventConfig() {
        return eventConfigRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "لم يتم العثور على إعدادات الحدث."));
    }

    @Override
    @Transactional
    public EventConfig updateEventConfig(
            EventConfig config, UUID adminId) {
        EventConfig existing = eventConfigRepository.findAll()
                .stream()
                .findFirst()
                .orElse(new EventConfig());

        if (config.getEventName() != null) {
            existing.setEventName(config.getEventName());
        }
        if (config.getEventDate() != null) {
            existing.setEventDate(config.getEventDate());
        }
        if (config.getDayStartTime() != null) {
            existing.setDayStartTime(config.getDayStartTime());
        }
        if (config.getAlertMinutes() != null) {
            existing.setAlertMinutes(config.getAlertMinutes());
        }

        userRepository.findById(adminId)
                .ifPresent(existing::setUpdatedBy);

        return eventConfigRepository.save(existing);
    }
}