package com.github.tunashred.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tunashred.dtos.MessageInfo;
import com.github.tunashred.kafka.ClientConsumer;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerWebSocketController {
    private final ClientConsumer kafkaConsumer;
    private final SimpMessagingTemplate messagingTemplate;

    @SneakyThrows
    public ConsumerWebSocketController(ClientConsumer kafkaConsumer, SimpMessagingTemplate messagingTemplate) {
        this.kafkaConsumer = kafkaConsumer;
        this.messagingTemplate = messagingTemplate;
        this.kafkaConsumer.setMessageHandler(this::handleKafkaRecord);
    }

    private void handleKafkaRecord(ConsumerRecord<String, String> record) {
        try {
            MessageInfo messageInfo = MessageInfo.deserialize(record.value());
            System.out.println("Deserialized: " + record.value());

            messagingTemplate.convertAndSend("/topic/group/" + messageInfo.getGroupChat().getChatID(), messageInfo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
