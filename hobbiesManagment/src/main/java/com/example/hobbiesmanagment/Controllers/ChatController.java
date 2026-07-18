package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.ChatMessagePayload;
import com.example.hobbiesmanagment.DTO.ConversationSummaryDto;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload ChatMessagePayload payload) {
        try {
            if (payload == null)
                throw new IllegalArgumentException("Message cannot be empty");

            if (payload.getRecipientId() == null)
                throw new IllegalArgumentException("Recipient cannot be empty");

            if (payload.getContent() == null || payload.getContent().isBlank())
                throw new IllegalArgumentException("Message content cannot be empty");

            chatMessageService.saveMessage(payload);

            messagingTemplate.convertAndSendToUser(
                    payload.getRecipientId().toString(),
                    "/queue/messages",
                    payload
            );

        } catch (IllegalArgumentException e) {
            messagingTemplate.convertAndSendToUser(
                    payload.getSenderId().toString(),
                    "/queue/errors",
                    "Invalid data: " + e.getMessage()
            );

        } catch (ResourceNotFoundException e) {
            messagingTemplate.convertAndSendToUser(
                    payload.getSenderId().toString(),
                    "/queue/errors",
                    "User not found: " + e.getMessage()
            );

        } catch (Exception e) {
            System.err.println("Failed to process chat message: " + e.getMessage());
            messagingTemplate.convertAndSendToUser(
                    payload.getSenderId().toString(),
                    "/queue/errors",
                    "Failed to send message, please try again"
            );
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessagePayload>> getChatMessages(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        if (userId1 <= 0 || userId2 <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        List<ChatMessagePayload> messages = chatMessageService.getMessagesBetweenUsers(userId1, userId2);
        return ResponseEntity.ok(messages); // always 200, empty list = no messages
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationSummaryDto>> getConversations(@PathVariable Long userId) {
        if (userId <= 0)
            throw new IllegalArgumentException("ID must be a positive number");

        List<ConversationSummaryDto> conversations = chatMessageService.getConversations(userId);
        return ResponseEntity.ok(conversations); // always 200, empty list = no conversations
    }
}