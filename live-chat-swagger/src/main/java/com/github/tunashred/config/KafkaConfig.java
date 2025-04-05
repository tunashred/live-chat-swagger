package com.github.tunashred.config;

import com.github.tunashred.dtos.GroupChat;
import com.github.tunashred.dtos.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

public class KafkaConfig {
    private static final Logger logger = LogManager.getLogger(KafkaConfig.class);

    public static final String unsafe_topic = "unsafe_chat";
    public static final String group_topic = "baia-mare";
    public static final String username = "gulios";

    public static final User user = new User(username);
    public static final GroupChat groupChat = new GroupChat(group_topic);

    public static Properties getProducerProps() {
        Properties producerProps = new Properties();
        try (InputStream propsFile = new FileInputStream("src/main/resources/producer.properties")) {
            producerProps.load(propsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return producerProps;
    }

    public static Properties getConsumerProps() {
        Properties consumerProps = new Properties();
        try (InputStream propsFile = new FileInputStream("src/main/resources/consumer.properties")) {
            consumerProps.load(propsFile);
            consumerProps.put(GROUP_ID_CONFIG, "consumer-" + username);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return consumerProps;
    }
}