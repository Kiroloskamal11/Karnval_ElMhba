package com.church.karneval.repository;

import com.church.karneval.enums.UserRole;
import com.church.karneval.enums.UserStatus;
import com.church.karneval.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByStatus(UserStatus status);

    boolean existsByTeamIdAndStatus(UUID teamId, UserStatus status);

    boolean existsByStationIdAndStatus(UUID stationId, UserStatus status);

    // ضيف
    List<User> findByRole(UserRole role);
}
