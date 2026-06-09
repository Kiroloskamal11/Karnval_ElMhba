package com.church.karneval.service.impl;

import com.church.karneval.enums.AgeGroup;
import com.church.karneval.enums.UserRole;
import com.church.karneval.model.Child;
import com.church.karneval.model.Team;
import com.church.karneval.model.User;
import com.church.karneval.repository.ChildRepository;
import com.church.karneval.repository.TeamRepository;
import com.church.karneval.repository.UserRepository;
import com.church.karneval.service.ChildService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChildServiceImpl implements ChildService {

    private final ChildRepository childRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public ChildServiceImpl(ChildRepository childRepository,
                            TeamRepository teamRepository,
                            UserRepository userRepository) {
        this.childRepository = childRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Child addChild(UUID teamId, String name, AgeGroup ageGroup, UUID addedBy) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على الفريق بالمعرف: " + teamId));

        User user = userRepository.findById(addedBy)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على المستخدم بالمعرف: " + addedBy));

        // Enforce that Team Leaders can only add children to their own team
        if (user.getRole() == UserRole.TEAM_LEADER) {
            if (user.getTeam() == null || !user.getTeam().getId().equals(teamId)) {
                throw new RuntimeException("غير مصرح لك بإضافة أطفال لفريق آخر.");
            }
        }

        Child child = new Child();
        child.setTeam(team);
        child.setName(name);
        child.setAgeGroup(ageGroup != null ? ageGroup : AgeGroup.OTHER);
        child.setAddedBy(user);

        return childRepository.save(child);
    }

    @Override
    @Transactional
    public Child updateChild(UUID childId, String name, AgeGroup ageGroup, UUID userId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على الطفل بالمعرف: " + childId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

        // Enforce that Team Leaders can only update children of their own team
        if (user.getRole() == UserRole.TEAM_LEADER) {
            if (user.getTeam() == null || !user.getTeam().getId().equals(child.getTeam().getId())) {
                throw new RuntimeException("غير مصرح لك بتعديل بيانات هذا الطفل.");
            }
        }

        if (name != null) {
            child.setName(name);
        }
        if (ageGroup != null) {
            child.setAgeGroup(ageGroup);
        }

        return childRepository.save(child);
    }

    @Override
    @Transactional
    public void deleteChild(UUID childId, UUID userId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على الطفل بالمعرف: " + childId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

        // Enforce that Team Leaders can only delete children of their own team
        if (user.getRole() == UserRole.TEAM_LEADER) {
            if (user.getTeam() == null || !user.getTeam().getId().equals(child.getTeam().getId())) {
                throw new RuntimeException("غير مصرح لك بحذف هذا الطفل.");
            }
        }
        childRepository.delete(child);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Child> getTeamChildren(UUID teamId) {
        return childRepository.findByTeamId(teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<AgeGroup, Long> getTeamStats(UUID teamId) {
        Map<AgeGroup, Long> stats = new EnumMap<>(AgeGroup.class);
        for (AgeGroup group : AgeGroup.values()) {
            stats.put(group, childRepository.countByTeamIdAndAgeGroup(teamId, group));
        }
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getEventStats() {
        Map<String, Object> stats = new HashMap<>();
        long total = childRepository.count();
        stats.put("total", total);

        Map<AgeGroup, Long> byAgeGroup = new EnumMap<>(AgeGroup.class);
        for (AgeGroup group : AgeGroup.values()) {
            byAgeGroup.put(group, childRepository.countByAgeGroup(group));
        }
        stats.put("byAgeGroup", byAgeGroup);

        return stats;
    }
}
