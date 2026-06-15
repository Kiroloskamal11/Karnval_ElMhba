package com.church.karneval.repository;

import com.church.karneval.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId " +
            "OR m.recipient.id = :userId " +
            "OR (m.recipientType = com.church.karneval.enums.MessageRecipientType.BROADCAST AND " +
            "    (m.sender.role = com.church.karneval.enums.UserRole.SUPER_ADMIN " +
            "     OR m.sender.role = com.church.karneval.enums.UserRole.ADMIN " +
            "     OR :isAdmin = true)) " +
            "OR (m.recipientType = com.church.karneval.enums.MessageRecipientType.ALL_TEAM_LEADERS AND :isTeamLeader = true) " +
            "OR (m.recipientType = com.church.karneval.enums.MessageRecipientType.ALL_CAMP_LEADERS AND :isCampLeader = true) " +
            "ORDER BY m.createdAt DESC")
    List<Message> findUserMessages(
            @Param("userId") UUID userId,
            @Param("isTeamLeader") boolean isTeamLeader,
            @Param("isCampLeader") boolean isCampLeader,
            @Param("isAdmin") boolean isAdmin);
}
