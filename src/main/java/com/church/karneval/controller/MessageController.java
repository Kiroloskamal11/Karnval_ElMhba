package com.church.karneval.controller;

import com.church.karneval.enums.MessageRecipientType;
import com.church.karneval.enums.UserRole;
import com.church.karneval.model.Message;
import com.church.karneval.model.User;
import com.church.karneval.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Message> sendMessage(
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User currentUser) {
        // Broadcast و Group — Admin و Super Admin بس (الـ BROADCAST مسموح للجميع لأنه يمثل طلبات دعم عند إرساله من القادة)
        if (request.getRecipientType() != MessageRecipientType.INDIVIDUAL && request.getRecipientType() != MessageRecipientType.BROADCAST) {
            if (currentUser.getRole() != UserRole.ADMIN &&
                    currentUser.getRole() != UserRole.SUPER_ADMIN) {
                return ResponseEntity.status(403).build();
            }
        }
        Message message = messageService.sendMessage(
                currentUser.getId(),
                request.getRecipientId(),
                request.getRecipientType(),
                request.getContent());
        return ResponseEntity.status(201).body(message);
    }

    @GetMapping
    public ResponseEntity<List<Message>> getUserMessages(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.getUserMessages(currentUser.getId()));
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Message> markAsRead(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.markAsRead(messageId, currentUser.getId()));
    }

    public static class SendMessageRequest {
        private UUID recipientId;
        private MessageRecipientType recipientType;
        private String content;

        public UUID getRecipientId() {
            return recipientId;
        }

        public void setRecipientId(UUID recipientId) {
            this.recipientId = recipientId;
        }

        public MessageRecipientType getRecipientType() {
            return recipientType;
        }

        public void setRecipientType(MessageRecipientType recipientType) {
            this.recipientType = recipientType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
