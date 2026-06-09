package com.church.karneval.controller;

import com.church.karneval.enums.UserRole;
import com.church.karneval.model.Arrival;
import com.church.karneval.model.User;
import com.church.karneval.service.ArrivalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/arrivals")
public class ArrivalController {

    private final ArrivalService arrivalService;

    public ArrivalController(ArrivalService arrivalService) {
        this.arrivalService = arrivalService;
    }

    @PostMapping
    public ResponseEntity<Arrival> confirmArrival(
            @RequestBody ConfirmArrivalRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.TEAM_LEADER) {
            return ResponseEntity.status(403).build();
        }
        Arrival arrival = arrivalService.confirmArrival(
                request.getTeamId(),
                request.getStationId(),
                currentUser.getId());
        return ResponseEntity.status(201).body(arrival);
    }

    @PutMapping("/{arrivalId}")
    public ResponseEntity<Arrival> updateArrival(
            @PathVariable UUID arrivalId,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.TEAM_LEADER) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(
                arrivalService.updateArrival(arrivalId,
                        currentUser.getId()));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<Arrival>> getTeamArrivals(@PathVariable UUID teamId) {
        return ResponseEntity.ok(arrivalService.getTeamArrivals(teamId));
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<Arrival>> getStationArrivals(@PathVariable UUID stationId) {
        return ResponseEntity.ok(arrivalService.getStationArrivals(stationId));
    }

    public static class ConfirmArrivalRequest {
        private UUID teamId;
        private UUID stationId;

        public UUID getTeamId() {
            return teamId;
        }

        public void setTeamId(UUID teamId) {
            this.teamId = teamId;
        }

        public UUID getStationId() {
            return stationId;
        }

        public void setStationId(UUID stationId) {
            this.stationId = stationId;
        }
    }
}
