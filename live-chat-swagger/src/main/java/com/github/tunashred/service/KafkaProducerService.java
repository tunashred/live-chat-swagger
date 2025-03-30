package com.github.tunashred.service;

import com.github.tunashred.dtos.GroupChat;
import com.github.tunashred.dtos.MessageInfo;
import com.github.tunashred.dtos.User;
import com.github.tunashred.kafka.ClientProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {
    private final ClientProducer kafkaProducer;

    @Autowired
    public KafkaProducerService(ClientProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public void sendMessage(String group, String user, String message) {
        MessageInfo messageInfo = new MessageInfo(new GroupChat(group), new User(user), message);

        kafkaProducer.sendMessage(messageInfo);
    }
}