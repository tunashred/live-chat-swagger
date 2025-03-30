//package com.github.tunashred.service;
//
//import com.github.tunashred.dtos.MessageInfo;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class KafkaConsumerService {
//    @KafkaListener(topics = "${client.groupname}", groupId = "swag")
//    public void listen(MessageInfo message) {
//        log.info("Received message: {}", message.toString());
//    }
//}
