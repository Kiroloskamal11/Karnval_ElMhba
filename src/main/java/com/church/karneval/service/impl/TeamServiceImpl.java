package com.church.karneval.service.impl;

import com.church.karneval.model.Team;
import com.church.karneval.repository.TeamRepository;
import com.church.karneval.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Team getTeamById(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على الفريق بالمعرف: " + id));
    }

    @Override
    @Transactional
    public Team createTeam(String name, String color) {
        Team team = new Team();
        team.setName(name);
        team.setColor(color);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public Team updateTeam(UUID id, String name, String color) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على الفريق بالمعرف: " + id));
        team.setName(name);
        team.setColor(color);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public void deleteTeam(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على الفريق بالمعرف: " + id));
        teamRepository.delete(team);
    }
}
