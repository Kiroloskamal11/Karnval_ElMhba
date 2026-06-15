package com.church.karneval.dto;

import com.church.karneval.enums.UserRole;
import com.church.karneval.enums.UserStatus;
import com.church.karneval.model.Team;
import com.church.karneval.model.Station;
import java.util.UUID;

public class AuthResponse {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private String message;
    private TeamInfo team;
    private StationInfo station;

    public AuthResponse(UUID id, String name, String email, UserRole role, UserStatus status, String message) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.message = message;
    }

    public AuthResponse(UUID id, String name, String email, UserRole role, UserStatus status, String message,
                        Team team, Station station) {
        this(id, name, email, role, status, message);
        if (team != null) {
            this.team = new TeamInfo(team.getId(), team.getName(), team.getColor(), team.getColorHex());
        }
        if (station != null) {
            this.station = new StationInfo(station.getId(), station.getName());
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public TeamInfo getTeam() {
        return team;
    }

    public StationInfo getStation() {
        return station;
    }

    // Nested DTO for team info
    public static class TeamInfo {
        private UUID id;
        private String name;
        private String color;
        private String colorHex;

        public TeamInfo(UUID id, String name, String color, String colorHex) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.colorHex = colorHex;
        }

        public UUID getId() { return id; }
        public String getName() { return name; }
        public String getColor() { return color; }
        public String getColorHex() { return colorHex; }
    }

    // Nested DTO for station info
    public static class StationInfo {
        private UUID id;
        private String name;

        public StationInfo(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public UUID getId() { return id; }
        public String getName() { return name; }
    }
}
