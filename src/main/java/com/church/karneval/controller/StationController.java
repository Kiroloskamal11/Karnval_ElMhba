package com.church.karneval.controller;

import com.church.karneval.enums.UserRole;
import com.church.karneval.model.Station;
import com.church.karneval.model.User;
import com.church.karneval.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable UUID id) {
        return ResponseEntity.ok(stationService.getStationById(id));
    }

    @PostMapping
    public ResponseEntity<Station> createStation(
            @RequestBody CreateStationRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        Station station = stationService.createStation(
                request.getName(), request.getDescription(), request.getLocationHint());
        return ResponseEntity.status(201).body(station);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Station> updateStation(
            @PathVariable UUID id,
            @RequestBody CreateStationRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(stationService.updateStation(
                id, request.getName(), request.getDescription(), request.getLocationHint()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }

    public static class CreateStationRequest {
        private String name;
        private String description;
        private String locationHint;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLocationHint() {
            return locationHint;
        }

        public void setLocationHint(String locationHint) {
            this.locationHint = locationHint;
        }
    }
}
