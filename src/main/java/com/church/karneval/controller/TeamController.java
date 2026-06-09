package com.church.karneval.controller;

import com.church.karneval.enums.UserRole;
import com.church.karneval.model.Team;
import com.church.karneval.model.User;
import com.church.karneval.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(
            @RequestBody CreateTeamRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        Team team = teamService.createTeam(request.getName(), request.getColor());
        return ResponseEntity.status(201).body(team);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam(
            @PathVariable UUID id,
            @RequestBody CreateTeamRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(teamService.updateTeam(id, request.getName(), request.getColor()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(403).build();
        }
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    public static class CreateTeamRequest {
        private String name;
        private String color;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}
