import { useState, useRef, useEffect } from "react";
import { useAskAiMutation } from "../api/Api";
import {
  Box,
  IconButton,
  TextField,
  Typography,
  Avatar,
  CircularProgress,
  Tooltip,
} from "@mui/material";
import SendIcon from "@mui/icons-material/Send";
import CloseIcon from "@mui/icons-material/Close";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import "./AiAdvisor.css";

// Suggested starter questions shown before the user types
const SUGGESTIONS = [
  "I want to learn programming — where do I start?",
  "Which skill is best for a beginner?",
  "I love music, what can I learn here?",
  "Help me find a mentor for cooking",
];

const BotAvatar = () => (
  <Avatar className="ai-bot-avatar">
    <AutoAwesomeIcon sx={{ fontSize: "1rem" }} />
  </Avatar>
);

export default function AiAdvisor() {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState([
    {
      role: "bot",
      text: "Hi! I'm your SkillMate AI advisor 🤖\nTell me what you'd like to learn and I'll guide you to the right skill and mentor.",
    },
  ]);

  const [askAi, { isLoading }] = useAskAiMutation();
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  // Auto-scroll to bottom when messages update
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // Focus input when panel opens
  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 100);
    }
  }, [open]);

  const handleSend = async (text) => {
    const trimmed = (text || input).trim();
    if (!trimmed || isLoading) return;

    const userMsg = { role: "user", text: trimmed };
    setMessages((prev) => [...prev, userMsg]);
    setInput("");

    try {
      const result = await askAi(trimmed).unwrap();
      setMessages((prev) => [...prev, { role: "bot", text: result.reply }]);
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          role: "bot",
          text: "Sorry, I couldn't reach the AI right now. Please try again in a moment.",
          isError: true,
        },
      ]);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const hasUserMessages = messages.some((m) => m.role === "user");

  return (
    <>
      {/* ===== Floating Button ===== */}
      <Tooltip
        title="Ask AI Advisor"
        placement="left"
        arrow
        disableHoverListener={open}
      >
        <button
          className={`ai-fab ${open ? "ai-fab--open" : ""}`}
          onClick={() => setOpen((p) => !p)}
          aria-label="Open AI advisor"
        >
          {open ? (
            <CloseIcon sx={{ fontSize: "1.4rem" }} />
          ) : (
            <>
              <AutoAwesomeIcon sx={{ fontSize: "1.3rem" }} />
              <span className="ai-fab-label">AI Advisor</span>
            </>
          )}
        </button>
      </Tooltip>

      {/* ===== Chat Panel ===== */}
      <div className={`ai-panel ${open ? "ai-panel--visible" : ""}`} role="dialog" aria-label="AI Skill Advisor">

        {/* Header */}
        <div className="ai-panel-header">
          <div className="ai-panel-header-left">
            <BotAvatar />
            <div>
              <Typography className="ai-panel-title">SkillMate AI</Typography>
              <Typography className="ai-panel-subtitle">Skill Advisor</Typography>
            </div>
          </div>
          <IconButton
            size="small"
            onClick={() => setOpen(false)}
            className="ai-panel-close"
            aria-label="Close AI advisor"
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </div>

        {/* Messages */}
        <div className="ai-messages">
          {messages.map((msg, i) => (
            <div
              key={i}
              className={`ai-message-row ${msg.role === "user" ? "ai-message-row--user" : "ai-message-row--bot"}`}
            >
              {msg.role === "bot" && <BotAvatar />}
              <div
                className={`ai-bubble ${
                  msg.role === "user" ? "ai-bubble--user" : "ai-bubble--bot"
                } ${msg.isError ? "ai-bubble--error" : ""}`}
              >
                {/* Render newlines from bot responses */}
                {msg.text.split("\n").map((line, j) => (
                  <span key={j}>
                    {line}
                    {j < msg.text.split("\n").length - 1 && <br />}
                  </span>
                ))}
              </div>
            </div>
          ))}

          {/* Loading indicator */}
          {isLoading && (
            <div className="ai-message-row ai-message-row--bot">
              <BotAvatar />
              <div className="ai-bubble ai-bubble--bot ai-bubble--typing">
                <span className="ai-typing-dot" />
                <span className="ai-typing-dot" />
                <span className="ai-typing-dot" />
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Suggestion chips — shown only before first user message */}
        {!hasUserMessages && (
          <div className="ai-suggestions">
            {SUGGESTIONS.map((s, i) => (
              <button
                key={i}
                className="ai-suggestion-chip"
                onClick={() => handleSend(s)}
                disabled={isLoading}
              >
                {s}
              </button>
            ))}
          </div>
        )}

        {/* Input */}
        <div className="ai-input-row">
          <TextField
            inputRef={inputRef}
            className="ai-text-field"
            placeholder="Ask about skills or mentors..."
            variant="outlined"
            size="small"
            multiline
            maxRows={3}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={isLoading}
            aria-label="Ask AI"
          />
          <IconButton
            className="ai-send-btn"
            onClick={() => handleSend()}
            disabled={!input.trim() || isLoading}
            aria-label="Send message"
          >
            {isLoading ? (
              <CircularProgress size={18} sx={{ color: "#fff" }} />
            ) : (
              <SendIcon fontSize="small" />
            )}
          </IconButton>
        </div>
      </div>
    </>
  );
}
