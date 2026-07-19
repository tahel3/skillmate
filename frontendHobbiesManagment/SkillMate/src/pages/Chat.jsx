import { useState, useEffect, useRef } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import { useGetChatMessagesQuery, useGetConversationsQuery } from "../api/Api";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import {
  Avatar,
  Box,
  CircularProgress,
  IconButton,
  Paper,
  TextField,
  Typography,
} from "@mui/material";
import SendIcon from "@mui/icons-material/Send";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import ChatBubbleOutlineIcon from "@mui/icons-material/ChatBubbleOutlined";
import "./Chat.css";

/* ─────────────────────────────────────────────
   Helpers
───────────────────────────────────────────── */
const formatTime = (ts) => {
  if (!ts) return "";
  return new Date(ts).toLocaleTimeString("en-GB", { hour: "2-digit", minute: "2-digit" });
};

const formatDate = (ts) => {
  if (!ts) return "";
  return new Date(ts).toLocaleDateString("en-GB", { day: "2-digit", month: "2-digit", year: "numeric" });
};

const groupByDate = (messages) =>
  messages.reduce((acc, msg) => {
    const key = formatDate(msg.timestamp);
    if (!acc[key]) acc[key] = [];
    acc[key].push(msg);
    return acc;
  }, {});

const initials = (name = "") => name.charAt(0).toUpperCase();

/* ─────────────────────────────────────────────
   Conversation List (Inbox sidebar)
───────────────────────────────────────────── */
function ConversationList({ conversations, isLoading, isError, error, selectedId, onSelect }) {

  if (isLoading)
    return (
      <Box className="inbox-loading">
        <CircularProgress size={24} sx={{ color: "#B85C38" }} />
      </Box>
    );

  if (isError)
    return (
      <Box className="inbox-empty">
        <Typography variant="body2" color="error" textAlign="center">
          Failed to load conversations
          {error?.status ? ` (${error.status})` : ""}
        </Typography>
      </Box>
    );

  if (!conversations || conversations.length === 0)
    return (
      <Box className="inbox-empty">
        <ChatBubbleOutlineIcon sx={{ fontSize: "2.5rem", color: "#d6c5bc", mb: 1 }} />
        <Typography variant="body2" color="text.secondary" textAlign="center">
          No conversations yet
        </Typography>
      </Box>
    );

  return (
    <ul className="inbox-list">
      {conversations.map((conv) => (
        <li
          key={conv.otherUserId}
          className={`inbox-item ${selectedId === conv.otherUserId ? "inbox-item--active" : ""}`}
          onClick={() => onSelect(conv)}
        >
          <Avatar className="inbox-avatar">{initials(conv.otherUserName)}</Avatar>
          <Box className="inbox-item-body">
            <Box className="inbox-item-top">
              <Typography className="inbox-name">{conv.otherUserName}</Typography>
              <Typography className="inbox-time">{formatTime(conv.lastMessageTime)}</Typography>
            </Box>
            <Typography className="inbox-preview" noWrap>
              {conv.lastMessage || "—"}
            </Typography>
          </Box>
        </li>
      ))}
    </ul>
  );
}

/* ─────────────────────────────────────────────
   Message Thread (right panel)
───────────────────────────────────────────── */
function MessageThread({ currentUserId, recipient }) {
  const [inputValue, setInputValue] = useState("");
  const [localMessages, setLocalMessages] = useState([]);
  const [stompClient, setStompClient] = useState(null);
  const [connected, setConnected] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);
  const messagesEndRef = useRef(null);

  // Keep a ref to the current recipient so the WebSocket handler
  // always sees the latest value without needing to reconnect.
  const recipientRef = useRef(recipient);
  useEffect(() => {
    recipientRef.current = recipient;
  }, [recipient]);

  const { data: chatHistory, isLoading } = useGetChatMessagesQuery(
    { userId1: currentUserId, userId2: recipient.otherUserId },
    { skip: !currentUserId || !recipient?.otherUserId }
  );

  // Load history whenever the selected conversation changes
  useEffect(() => {
    if (chatHistory) setLocalMessages(chatHistory);
    else setLocalMessages([]);
  }, [chatHistory, recipient?.otherUserId]);

  // Scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [localMessages]);

  // WebSocket connection — created ONCE per currentUserId, never torn down
  // on conversation switch. The message handler reads recipientRef so it
  // always routes to the currently open thread.
  useEffect(() => {
    if (!currentUserId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8080/ws-chat"),
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);
        // Subscribe to personal topic — server publishes here for both sender and recipient
        client.subscribe(`/topic/messages/${currentUserId}`, (frame) => {
          const msg = JSON.parse(frame.body);
          const currentRecipientId = Number(recipientRef.current?.otherUserId);
          // Only render the message if it belongs to the currently open conversation
          if (
            Number(msg.senderId) === currentRecipientId ||
            Number(msg.recipientId) === currentRecipientId
          ) {
            setLocalMessages((prev) => {
              // Replace the optimistic local message (no id) with the server-confirmed one
              const withoutOptimistic = prev.filter(
                (m) => !(m._local && m.content === msg.content && Number(m.senderId) === Number(msg.senderId))
              );
              return [...withoutOptimistic, msg];
            });
          }
        });
        client.subscribe(`/topic/errors/${currentUserId}`, (frame) => {
          setErrorMsg(frame.body);
          setTimeout(() => setErrorMsg(null), 4000);
        });
      },
      onDisconnect: () => setConnected(false),
    });

    client.activate();
    setStompClient(client);
    return () => client.deactivate();
  }, [currentUserId]); // ← dependency is only currentUserId, NOT recipient

  const handleSend = () => {
    const trimmed = inputValue.trim();
    if (!trimmed || !stompClient || !connected) return;

    const payload = {
      senderId: Number(currentUserId),
      recipientId: Number(recipient.otherUserId),
      content: trimmed,
    };

    stompClient.publish({
      destination: "/app/chat.sendMessage",
      body: JSON.stringify(payload),
    });

    // Optimistically add the message locally — recipient may be offline,
    // but the server saves it and they will see it when they connect.
    setLocalMessages((prev) => [
      ...prev,
      { ...payload, timestamp: new Date().toISOString(), _local: true },
    ]);
    setInputValue("");
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const grouped = groupByDate(localMessages);

  return (
    <>
      {/* Thread header */}
      <Box className="thread-header">
        <Avatar className="thread-avatar">{initials(recipient.otherUserName)}</Avatar>
        <Box>
          <Typography className="thread-name">{recipient.otherUserName}</Typography>
          <Typography className="thread-status">
            <span className={`status-dot ${connected ? "online" : "offline"}`} />
            {connected ? "Connected" : "Connecting..."}
          </Typography>
        </Box>
      </Box>

      {/* Messages */}
      <Box className="chat-messages-area">
        {isLoading && (
          <Box className="chat-loading">
            <CircularProgress size={28} sx={{ color: "#B85C38" }} />
          </Box>
        )}

        {!isLoading && localMessages.length === 0 && (
          <Box className="chat-empty-state">
            <Typography variant="body2" color="text.secondary">
              No messages yet. Say hello! 👋
            </Typography>
          </Box>
        )}

        {Object.entries(grouped).map(([date, msgs]) => (
          <Box key={date}>
            <Box className="chat-date-divider">
              <span className="chat-date-label">{date}</span>
            </Box>
            {msgs.map((msg, i) => {
              const isMine = Number(msg.senderId) === Number(currentUserId);
              return (
                <Box
                  key={msg.id ?? `local-${i}`}
                  className={`chat-bubble-wrapper ${isMine ? "mine" : "theirs"}`}
                >
                  {!isMine && (
                    <Avatar className="chat-bubble-avatar">
                      {initials(recipient.otherUserName)}
                    </Avatar>
                  )}
                  <Box className={`chat-bubble ${isMine ? "bubble-mine" : "bubble-theirs"}`}>
                    <Typography className="bubble-content">{msg.content}</Typography>
                    <Typography className="bubble-time">{formatTime(msg.timestamp)}</Typography>
                  </Box>
                </Box>
              );
            })}
          </Box>
        ))}

        {errorMsg && (
          <Box className="chat-error-toast">
            <Typography variant="body2">{errorMsg}</Typography>
          </Box>
        )}

        <div ref={messagesEndRef} />
      </Box>

      {/* Input */}
      <Box className="chat-input-area">
        <TextField
          className="chat-text-field"
          placeholder={connected ? "Type a message..." : "Connecting..."}
          variant="outlined"
          size="small"
          multiline
          maxRows={4}
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={!connected}
        />
        <IconButton
          className="chat-send-btn"
          onClick={handleSend}
          disabled={!inputValue.trim() || !connected}
          aria-label="Send message"
        >
          <SendIcon />
        </IconButton>
      </Box>
    </>
  );
}

/* ─────────────────────────────────────────────
   Main Chat Page
───────────────────────────────────────────── */
export default function Chat() {
  const location = useLocation();
  const navigate = useNavigate();
  const reduxUserId = useSelector((state) => state.auth.userId);
  // Fallback to sessionStorage in case Redux hasn't rehydrated yet on first render
  // Parse carefully: both Redux and sessionStorage store userId as string
  const rawId = reduxUserId ?? sessionStorage.getItem("userId");
  const currentUserId = rawId ? Number(rawId) : null;
  console.log("[Chat] rawId:", rawId, "currentUserId:", currentUserId);

  // Fetch conversations here in the parent so the query always has the correct userId
  const {
    data: conversations,
    isLoading: convsLoading,
    isError: convsError,
    error: convsErrorObj,
  } = useGetConversationsQuery(currentUserId, {
    skip: !currentUserId || isNaN(currentUserId) || currentUserId <= 0,
    pollingInterval: 15000,
    refetchOnMountOrArgChange: true,
  });

  // Debug: log conversations whenever they change
  useEffect(() => {
    console.log("[Chat] conversations from server:", conversations);
  }, [conversations]);

  // Pre-selected conversation from MentorProfile navigate state
  const preSelectedId = location.state?.recipientId
    ? Number(location.state.recipientId)
    : null;

  const preSelected = preSelectedId
    ? {
        otherUserId: preSelectedId,
        otherUserName: location.state.recipientName || "Mentor",
      }
    : null;

  const [selectedConv, setSelectedConv] = useState(preSelected);

  // Once conversations load from the server, enrich the preSelected entry
  // (so it gets the full data from the server and shows as active in the sidebar)
  useEffect(() => {
    if (!preSelectedId || !conversations?.length) return;
    const match = conversations.find((c) => Number(c.otherUserId) === preSelectedId);
    if (match) setSelectedConv(match);
  }, [conversations, preSelectedId]);

  return (
    <Box className="chat-page-wrapper">
      <Paper className="chat-layout" elevation={3}>

        {/* ── LEFT: Inbox ── */}
        <Box className="chat-sidebar">
          <Box className="sidebar-header">
            <IconButton size="small" onClick={() => navigate(-1)} aria-label="Go back">
              <ArrowBackIcon fontSize="small" />
            </IconButton>
            <Typography className="sidebar-title">Messages</Typography>
          </Box>
          <ConversationList
            conversations={conversations}
            isLoading={convsLoading}
            isError={convsError}
            error={convsErrorObj}
            selectedId={selectedConv?.otherUserId}
            onSelect={setSelectedConv}
          />
        </Box>

        {/* ── RIGHT: Thread or placeholder ── */}
        <Box className="chat-thread">
          {selectedConv ? (
            <MessageThread currentUserId={currentUserId} recipient={selectedConv} />
          ) : (
            <Box className="thread-placeholder">
              <ChatBubbleOutlineIcon sx={{ fontSize: "3.5rem", color: "#d6c5bc", mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                Select a conversation
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                Choose a conversation from the left, or open a chat from a mentor's profile.
              </Typography>
            </Box>
          )}
        </Box>

      </Paper>
    </Box>
  );
}
