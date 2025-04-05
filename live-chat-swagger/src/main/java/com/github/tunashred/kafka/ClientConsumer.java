package com.github.tunashred.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tunashred.config.KafkaConfig;
import com.github.tunashred.dtos.MessageInfo;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientConsumer {
    private static final Logger logger = LogManager.getLogger(ClientConsumer.class);

    private final Consumer<String, String> consumer;
    private List<ConsumerRecord<String, String>> records = new ArrayList<>();

    public ClientConsumer() {
        this.consumer = new KafkaConsumer<>(KafkaConfig.getConsumerProps());
        this.consumer.subscribe(Collections.singletonList(KafkaConfig.group_topic));
    }

    private MessageInfo consumeStoredMessage() throws JsonProcessingException {
        if (records.isEmpty()) {
            return null;
        }
        MessageInfo message = MessageInfo.deserialize(records.get(0).value());
        records.remove(0);
        return message;
    }

    // TODO: reminder to call this somewhere
    public void close() {
        consumer.close();
    }

    //
    // Client API
    //

    public List<String> consumeMessages(Duration timeout) {
        ConsumerRecords<String, String> records = consumer.poll(timeout);
        List<String> messages = new ArrayList<>();
        records.forEach(record -> messages.add(record.value()));
        return messages;
    }

    public MessageInfo consumeMessage() {
        MessageInfo message;
        final int maxTries = 4;
        try {
            if (!records.isEmpty()) {
                return consumeStoredMessage();
            }

            ConsumerRecords<String, String> consumerRecords;
            int tries = 0;
            do {
                consumerRecords = consumer.poll(Duration.ofMillis(1000));
                tries++;
                if (tries >= maxTries) {
                    break;
                }
                System.out.println("da loop");
            } while (consumerRecords.isEmpty());
            System.out.println("outta loop");

            for (ConsumerRecord<String, String> record : consumerRecords) {
                this.records.add(record);
            }
            consumer.commitSync();

            return consumeStoredMessage();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
