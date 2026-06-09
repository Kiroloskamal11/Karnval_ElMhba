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

    // ✅ الصح
    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId " +
            "OR m.recipient.id = :userId " +
            "OR m.recipientType != com.church.karneval.enums.MessageRecipientType.INDIVIDUAL " +
            "ORDER BY m.createdAt DESC")
    List<Message> findUserMessages(@Param("userId") UUID userId);
}
