package com.church.karneval.controller;

import com.church.karneval.enums.AgeGroup;
import com.church.karneval.enums.UserRole;
import com.church.karneval.model.Child;
import com.church.karneval.model.User;
import com.church.karneval.service.ChildService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/children")
public class ChildController {

    private final ChildService childService;

    public ChildController(ChildService childService) {
        this.childService = childService;
    }

    @PostMapping
    public ResponseEntity<Child> addChild(
            @RequestBody AddChildRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.TEAM_LEADER) {
            return ResponseEntity.status(403).build();
        }
        Child child = childService.addChild(
                request.getTeamId(), request.getName(),
                request.getAgeGroup(), currentUser.getId());
        return ResponseEntity.status(201).body(child);
    }

    @PutMapping("/{childId}")
    public ResponseEntity<Child> updateChild(
            @PathVariable UUID childId,
            @RequestBody UpdateChildRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.TEAM_LEADER) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(
                childService.updateChild(childId,
                        request.getName(), request.getAgeGroup(), currentUser.getId()));
    }

    @DeleteMapping("/{childId}")
    public ResponseEntity<Void> deleteChild(
            @PathVariable UUID childId,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.TEAM_LEADER) {
            return ResponseEntity.status(403).build();
        }
        childService.deleteChild(childId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<Child>> getTeamChildren(@PathVariable UUID teamId) {
        return ResponseEntity.ok(childService.getTeamChildren(teamId));
    }

    @GetMapping("/team/{teamId}/stats")
    public ResponseEntity<Map<AgeGroup, Long>> getTeamStats(@PathVariable UUID teamId) {
        return ResponseEntity.ok(childService.getTeamStats(teamId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEventStats(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN && currentUser.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(childService.getEventStats());
    }

    public static class AddChildRequest {
        private UUID teamId;
        private String name;
        private AgeGroup ageGroup;

        public UUID getTeamId() {
            return teamId;
        }

        public void setTeamId(UUID teamId) {
            this.teamId = teamId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AgeGroup getAgeGroup() {
            return ageGroup;
        }

        public void setAgeGroup(AgeGroup ageGroup) {
            this.ageGroup = ageGroup;
        }
    }

    public static class UpdateChildRequest {
        private String name;
        private AgeGroup ageGroup;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AgeGroup getAgeGroup() {
            return ageGroup;
        }

        public void setAgeGroup(AgeGroup ageGroup) {
            this.ageGroup = ageGroup;
        }
    }
}
