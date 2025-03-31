package com.github.tunashred.config;

import com.github.tunashred.dtos.GroupChat;
import com.github.tunashred.dtos.User;
import com.github.tunashred.kafka.ClientProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${client.username}")
    private String username;

    @Value("${client.groupname}")
    private String groupname;

    @Bean
    public ClientProducer producer() {
        ClientProducer producer = new ClientProducer(new User(username), new GroupChat(groupname));

        Thread producerThread = new Thread(producer);
        producerThread.setDaemon(true);
        producerThread.start();

        return producer;
    }
}
