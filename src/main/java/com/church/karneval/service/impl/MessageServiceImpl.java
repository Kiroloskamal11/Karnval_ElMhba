package com.church.karneval.service.impl;

import com.church.karneval.enums.MessageRecipientType;
import com.church.karneval.enums.UserRole;
import com.church.karneval.model.Message;
import com.church.karneval.model.User;
import com.church.karneval.repository.MessageRepository;
import com.church.karneval.repository.UserRepository;
import com.church.karneval.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Message sendMessage(UUID senderId, UUID recipientId, MessageRecipientType type, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على المرسل بالمعرف: " + senderId));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipientType(type != null ? type : MessageRecipientType.INDIVIDUAL);
        message.setContent(content);
        message.setIsRead(false);

        if (message.getRecipientType() == MessageRecipientType.INDIVIDUAL && recipientId != null) {
            User recipient = userRepository.findById(recipientId)
                    .orElseThrow(() -> new RuntimeException("لم يتم العثور على المستلم بالمعرف: " + recipientId));
            
            // Enforce that Team Leaders and Camp Leaders can only send individual messages to Admins or Super Admins
            if (sender.getRole() == UserRole.TEAM_LEADER || sender.getRole() == UserRole.CAMP_LEADER) {
                if (recipient.getRole() != UserRole.ADMIN && recipient.getRole() != UserRole.SUPER_ADMIN) {
                    throw new RuntimeException("غير مصرح لك بإرسال رسائل مباشرة لغير المسؤولين.");
                }
            }
            message.setRecipient(recipient);
        } else if (recipientId != null) {
            // For other recipient types, we can optionally associate a recipient if one is
            // specified, but typically they are null.
            userRepository.findById(recipientId).ifPresent(message::setRecipient);
        }

        return messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getUserMessages(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("لم يتم العثور على المستخدم بالمعرف: " + userId);
        }
        return messageRepository.findUserMessages(userId);
    }

    @Override
    @Transactional
    public Message markAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException(
                        "لم يتم العثور على الرسالة بالمعرف: " + messageId));

        // تحقق إن المستخدم هو المستلم
        if (message.getRecipient() != null &&
                !message.getRecipient().getId().equals(userId)) {
            throw new RuntimeException("غير مصرح لك بتعديل هذه الرسالة");
        }

        message.setIsRead(true);
        return messageRepository.save(message);
    }
}
