package com.church.karneval.controller;

import com.church.karneval.enums.UserRole;
import com.church.karneval.model.EventConfig;
import com.church.karneval.model.User;
import com.church.karneval.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
public class EventConfigController {

    private final ScheduleService scheduleService;

    public EventConfigController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<EventConfig> getEventConfig() {
        return ResponseEntity.ok(scheduleService.getEventConfig());
    }

    @PutMapping
    public ResponseEntity<EventConfig> updateEventConfig(
            @RequestBody EventConfig config,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        EventConfig updated = scheduleService.updateEventConfig(
                config, currentUser.getId());
        return ResponseEntity.ok(updated);
    }
}
