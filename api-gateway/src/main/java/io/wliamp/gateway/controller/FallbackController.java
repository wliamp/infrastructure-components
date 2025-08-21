package io.wliamp.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {
    @RequestMapping("/fallback/issue")
    public ResponseEntity<String> fallbackIssue() {
        return ResponseEntity.status(503).body("Issue Token service is unavailable");
    }

    @RequestMapping("/fallback/verify")
    public ResponseEntity<String> fallbackVerify() {
        return ResponseEntity.status(503).body("Verify Third-Party service is unavailable");
    }

    @RequestMapping("/fallback/auth")
    public ResponseEntity<String> fallbackAuth() {
        return ResponseEntity.status(503).body("Authenticate service is unavailable");
    }

    @RequestMapping("/fallback/game")
    public ResponseEntity<String> fallbackGame() {
        return ResponseEntity.status(503).body("Game service is unavailable");
    }

    @RequestMapping("/fallback/chat")
    public ResponseEntity<String> fallbackChat() {
        return ResponseEntity.status(503).body("Chat service is unavailable");
    }
}
