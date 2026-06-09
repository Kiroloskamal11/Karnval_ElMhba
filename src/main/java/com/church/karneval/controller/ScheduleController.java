package com.church.karneval.controller;

import com.church.karneval.dto.TeamLiveStatus;
import com.church.karneval.model.ScheduleSlot;
import com.church.karneval.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.church.karneval.enums.UserRole;
import com.church.karneval.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<Map<UUID, List<ScheduleSlot>>> getFullSchedule() {
        return ResponseEntity.ok(scheduleService.getFullSchedule());
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<ScheduleSlot>> getTeamSchedule(@PathVariable UUID teamId) {
        return ResponseEntity.ok(scheduleService.getTeamSchedule(teamId));
    }

    @GetMapping("/team/{teamId}/current")
    public ResponseEntity<ScheduleSlot> getCurrentSlot(@PathVariable UUID teamId) {
        ScheduleSlot slot = scheduleService.getCurrentSlot(teamId);
        return slot != null ? ResponseEntity.ok(slot) : ResponseEntity.noContent().build();
    }

    @GetMapping("/team/{teamId}/next")
    public ResponseEntity<ScheduleSlot> getNextSlot(@PathVariable UUID teamId) {
        ScheduleSlot slot = scheduleService.getNextSlot(teamId);
        return slot != null ? ResponseEntity.ok(slot) : ResponseEntity.noContent().build();
    }

    @GetMapping("/team/{teamId}/live")
    public ResponseEntity<TeamLiveStatus> getTeamLiveStatus(
            @PathVariable UUID teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime currentTime) {
        if (currentTime != null) {
            return ResponseEntity.ok(scheduleService.getTeamLiveStatus(teamId, currentTime));
        }
        return ResponseEntity.ok(scheduleService.getTeamLiveStatus(teamId));
    }

    @GetMapping("/live")
    public ResponseEntity<List<TeamLiveStatus>> getAllTeamsLiveStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime currentTime) {
        if (currentTime != null) {
            return ResponseEntity.ok(scheduleService.getAllTeamsLiveStatus(currentTime));
        }
        return ResponseEntity.ok(scheduleService.getAllTeamsLiveStatus());
    }

    @PutMapping("/slots/{slotId}")
    public ResponseEntity<ScheduleSlot> updateSlot(
            @PathVariable UUID slotId,
            @RequestBody UpdateSlotRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(
                scheduleService.updateSlot(slotId,
                        request.getStartTime(), request.getEndTime()));
    }

    public static class UpdateSlotRequest {
        private LocalTime startTime;
        private LocalTime endTime;

        public LocalTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }
    }
}
