package com.church.karneval.service;

import com.church.karneval.model.Team;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    List<Team> getAllTeams();

    Team getTeamById(UUID id);

    Team createTeam(String name, String color);

    Team updateTeam(UUID id, String name, String color);

    void deleteTeam(UUID id);
}
