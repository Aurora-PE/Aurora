package ro.unibuc.prodeng.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ro.unibuc.prodeng.request.SendMessageRequest;
import ro.unibuc.prodeng.response.MessageResponse;
import ro.unibuc.prodeng.service.MessageService;
import ro.unibuc.prodeng.service.MetricsService;
import ro.unibuc.prodeng.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final MetricsService metricsService;

    public MessageController(MessageService messageService, MetricsService metricsService) {
        this.messageService = messageService;
        this.metricsService = metricsService;
    }

    @PostMapping("/{receiverId}")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String receiverId,
            @RequestBody SendMessageRequest request
    ) {

        String senderId = JwtUtil.extractRequesterId(authHeader);

        metricsService.recordMessagesCreated();

        return ResponseEntity.ok(
                messageService.sendMessage(senderId, receiverId, request)
        );
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId
    ) {

        String requesterId = JwtUtil.extractRequesterId(authHeader);

        return ResponseEntity.ok(
                messageService.getConversationMessages(requesterId, conversationId)
        );
    }

    @PatchMapping("/{messageId}/read")
    public ResponseEntity<MessageResponse> markMessageRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String messageId
    ) {

        String requesterId = JwtUtil.extractRequesterId(authHeader);

        return ResponseEntity.ok(
                messageService.markMessageRead(requesterId, messageId)
        );
    }
}
