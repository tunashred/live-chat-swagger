package com.github.tunashred.controller;

import com.github.tunashred.dtos.MessageInfo;
import com.github.tunashred.kafka.ClientConsumer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/messages")
public class MessageGetterController {
    private final ClientConsumer clientConsumer;
    private final Map<String, Long> offsets = new ConcurrentHashMap<>();

    public MessageGetterController(ClientConsumer clientConsumer) {
        this.clientConsumer = clientConsumer;
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<List<MessageInfo>> getMessages(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "10") int limit) {

        List<MessageInfo> messages = clientConsumer.getMessages(groupId, -2L, limit);

        return ResponseEntity.ok(messages);
    }
}
