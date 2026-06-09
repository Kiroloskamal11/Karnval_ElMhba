package com.church.karneval.service;

import com.church.karneval.enums.MessageRecipientType;
import com.church.karneval.model.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message sendMessage(UUID senderId, UUID recipientId, MessageRecipientType type, String content);

    List<Message> getUserMessages(UUID userId);

    Message markAsRead(UUID messageId, UUID userId);
}
