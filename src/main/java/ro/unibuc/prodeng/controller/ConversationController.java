package ro.unibuc.prodeng.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.response.ConversationResponse;
import ro.unibuc.prodeng.service.ConversationService;
import ro.unibuc.prodeng.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getMyConversations(
            @RequestHeader("Authorization") String authHeader) {

        String requesterId = JwtUtil.extractRequesterId(authHeader);

        return ResponseEntity.ok(
                conversationService.getUserConversations(requesterId)
        );
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId) {

        String requesterId = JwtUtil.extractRequesterId(authHeader);

        return ResponseEntity.ok(
                conversationService.getConversation(requesterId, conversationId)
        );
    }
}