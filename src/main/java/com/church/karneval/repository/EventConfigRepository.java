package com.church.karneval.repository;

import com.church.karneval.model.EventConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventConfigRepository extends JpaRepository<EventConfig, UUID> {
}
