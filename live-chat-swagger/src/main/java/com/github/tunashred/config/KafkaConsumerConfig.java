package com.github.tunashred.config;

import com.github.tunashred.kafka.ClientConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${client.username}")
    private String username;

    @Value("${client.groupname}")
    private String groupname;

    @Bean
    public ClientConsumer consumer() {
        return new ClientConsumer(username, groupname);
    }
}
