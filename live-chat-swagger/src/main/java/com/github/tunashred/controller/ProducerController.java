package com.github.tunashred.controller;

import com.github.tunashred.dto.MessageRequest;
import com.github.tunashred.dtos.GroupChat;
import com.github.tunashred.dtos.MessageInfo;
import com.github.tunashred.dtos.User;
import com.github.tunashred.kafka.ClientProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class ProducerController {
    private final ClientProducer kafkaProducer;

    public ProducerController(ClientProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping("/send")
    @Operation(summary = "Send message to a group")
    public ResponseEntity<String> sendMessage(
            @RequestBody MessageRequest messageContent) {

        MessageInfo messageInfo = new MessageInfo(new GroupChat(messageContent.getGroupName()), new User(messageContent.getUserName()), messageContent.getMessageContent());

        kafkaProducer.sendMessage(messageInfo);

        return ResponseEntity.ok("Message sent successfully");
    }
}
