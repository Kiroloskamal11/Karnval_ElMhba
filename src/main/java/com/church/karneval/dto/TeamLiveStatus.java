package com.church.karneval.dto;

import java.util.UUID;

public class TeamLiveStatus {
    private UUID teamId;
    private String teamName;
    private String teamColor;
    private String teamColorHex;
    private String currentStationName;
    private String currentStationLocation;
    private String nextStationName;
    private String nextStationLocation;
    private Long timeRemainingSeconds;
    private Boolean isAlertTime; // If remaining minutes <= alertConfigMinutes

    public TeamLiveStatus() {}

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(String teamColor) {
        this.teamColor = teamColor;
    }

    public String getTeamColorHex() {
        return teamColorHex;
    }

    public void setTeamColorHex(String teamColorHex) {
        this.teamColorHex = teamColorHex;
    }

    public String getCurrentStationName() {
        return currentStationName;
    }

    public void setCurrentStationName(String currentStationName) {
        this.currentStationName = currentStationName;
    }

    public String getCurrentStationLocation() {
        return currentStationLocation;
    }

    public void setCurrentStationLocation(String currentStationLocation) {
        this.currentStationLocation = currentStationLocation;
    }

    public String getNextStationName() {
        return nextStationName;
    }

    public void setNextStationName(String nextStationName) {
        this.nextStationName = nextStationName;
    }

    public String getNextStationLocation() {
        return nextStationLocation;
    }

    public void setNextStationLocation(String nextStationLocation) {
        this.nextStationLocation = nextStationLocation;
    }

    public Long getTimeRemainingSeconds() {
        return timeRemainingSeconds;
    }

    public void setTimeRemainingSeconds(Long timeRemainingSeconds) {
        this.timeRemainingSeconds = timeRemainingSeconds;
    }

    public Boolean getIsAlertTime() {
        return isAlertTime;
    }

    public void setIsAlertTime(Boolean alertTime) {
        isAlertTime = alertTime;
    }
}
