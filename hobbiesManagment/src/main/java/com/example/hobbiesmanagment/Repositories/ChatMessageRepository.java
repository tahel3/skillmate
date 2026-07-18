package com.example.hobbiesmanagment.Repositories;

import com.example.hobbiesmanagment.Entities.Availability;
import com.example.hobbiesmanagment.Entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
            Long senderId1, Long recipientId1, Long senderId2, Long recipientId2);

    /**
     * Returns the IDs of all users that have exchanged at least one message with userId.
     * Used to build the conversations list (inbox).
     */
    @Query("""
        SELECT DISTINCT
            CASE WHEN m.sender.id = :userId THEN m.recipient.id ELSE m.sender.id END
        FROM ChatMessage m
        WHERE m.sender.id = :userId OR m.recipient.id = :userId
    """)
    List<Long> findDistinctConversationPartnerIds(@Param("userId") Long userId);

    /**
     * Returns the single most recent message exchanged between two users.
     */
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.sender.id = :userId AND m.recipient.id = :otherId)
           OR (m.sender.id = :otherId AND m.recipient.id = :userId)
        ORDER BY m.timestamp DESC
        LIMIT 1
    """)
    ChatMessage findLatestMessageBetween(@Param("userId") Long userId, @Param("otherId") Long otherId);
}
