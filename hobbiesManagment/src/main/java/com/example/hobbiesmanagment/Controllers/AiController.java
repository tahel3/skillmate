package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.AiRequest;
import com.example.hobbiesmanagment.DTO.AiResponse;
import com.example.hobbiesmanagment.Service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiService geminiService;

    /**
     * POST /api/ai/ask
     * Body: { "message": "..." }
     * Returns: { "reply": "..." }
     */
    @PostMapping("/ask")
    public ResponseEntity<AiResponse> ask(@RequestBody AiRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new AiResponse("Please provide a message."));
        }

        String reply = geminiService.ask(request.getMessage().trim());
        return ResponseEntity.ok(new AiResponse(reply));
    }
}
