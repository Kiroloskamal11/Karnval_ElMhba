package com.church.karneval.service;

import com.church.karneval.enums.AgeGroup;
import com.church.karneval.model.Child;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ChildService {
    Child addChild(UUID teamId, String name, AgeGroup ageGroup, UUID addedBy);
    Child updateChild(UUID childId, String name, AgeGroup ageGroup, UUID userId);
    void deleteChild(UUID childId, UUID userId);
    List<Child> getTeamChildren(UUID teamId);
    Map<AgeGroup, Long> getTeamStats(UUID teamId);
    Map<String, Object> getEventStats();
}
