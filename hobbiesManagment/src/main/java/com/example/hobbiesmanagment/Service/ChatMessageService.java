package com.example.hobbiesmanagment.Service;
import com.example.hobbiesmanagment.DTO.ChatMessagePayload;
import com.example.hobbiesmanagment.DTO.ChatMessageSaveResponseDto;
import com.example.hobbiesmanagment.DTO.ConversationSummaryDto;
import com.example.hobbiesmanagment.Entities.ChatMessage;
import com.example.hobbiesmanagment.Entities.User;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Repositories.ChatMessageRepository;
import com.example.hobbiesmanagment.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Receives the raw chat payload, processes it, and saves it to the database
     */
    @Transactional
    public ChatMessageSaveResponseDto saveMessage(ChatMessagePayload payload) {
        User sender = userRepository.findById(payload.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with ID: " + payload.getSenderId()));
        User recipient = userRepository.findById(payload.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with ID: " + payload.getRecipientId()));
        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(payload.getContent());
        message.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(message);
        return ChatMessageSaveResponseDto.builder()
                .id(savedMessage.getId())
                .senderId(savedMessage.getSender().getId())
                .recipientId(savedMessage.getRecipient().getId())
                .content(savedMessage.getContent())
                .timestamp(savedMessage.getTimestamp())
                .build();
    }

    public List<ChatMessagePayload> getMessagesBetweenUsers(Long userId1, Long userId2) {
        return chatMessageRepository
                .findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
                        userId1, userId2, userId2, userId1)
                .stream()
                .map(msg -> modelMapper.map(msg, ChatMessagePayload.class))
                .collect(Collectors.toList());
    }

    /**
     * Returns a summary of all conversations for a given user,
     * sorted by most recent message first.
     */
    public List<ConversationSummaryDto> getConversations(Long userId) {
        List<Long> partnerIds = chatMessageRepository.findDistinctConversationPartnerIds(userId);

        return partnerIds.stream()
                .map(partnerId -> {
                    User partner = userRepository.findById(partnerId).orElse(null);
                    if (partner == null) return null;

                    ChatMessage latest = chatMessageRepository.findLatestMessageBetween(userId, partnerId);

                    // Resolve display name: prefer full name from credential, fall back to email
                    String name = resolveDisplayName(partner);

                    return ConversationSummaryDto.builder()
                            .otherUserId(partnerId)
                            .otherUserName(name)
                            .lastMessage(latest != null ? latest.getContent() : "")
                            .lastMessageTime(latest != null ? latest.getTimestamp() : LocalDateTime.MIN)
                            .build();
                })
                .filter(dto -> dto != null)
                .sorted(Comparator.comparing(ConversationSummaryDto::getLastMessageTime).reversed())
                .collect(Collectors.toList());
    }

    private String resolveDisplayName(User user) {
        // Try credential name first
        if (user.getCredential() != null) {
            try {
                // Credential is an @Embedded object — access name via reflection-safe getter if available
                String name = user.getCredential().getName();
                if (name != null && !name.isBlank()) return name;
            } catch (Exception ignored) {}
        }
        // Fall back to email prefix
        String email = user.getEmail();
        if (email != null && email.contains("@")) return email.split("@")[0];
        return "User #" + user.getId();
    }
}