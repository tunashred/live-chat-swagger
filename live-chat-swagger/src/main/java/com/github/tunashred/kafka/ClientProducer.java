package com.github.tunashred.kafka;

import com.github.tunashred.config.KafkaConfig;
import com.github.tunashred.dtos.MessageInfo;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClientProducer {
    private static final Logger logger = LogManager.getLogger(ClientProducer.class);
    private final Producer<String, String> producer;

    public ClientProducer() {
        this.producer = new KafkaProducer<>(KafkaConfig.getProducerProps());
    }

    // TODO: reminder to call this somewhere
    public void close() {
        producer.close();
    }

    //
    // Client API
    //

    public void sendMessage(String message) {
        MessageInfo messageInfo = new MessageInfo(KafkaConfig.groupChat, KafkaConfig.user, message);
        try {
            String serialized = MessageInfo.serialize(messageInfo);
            ProducerRecord<String, String> record = new ProducerRecord<>(KafkaConfig.unsafe_topic, messageInfo.getGroupChat().getChatID(), serialized);

            producer.send(record);
            producer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //
    // Admin API
    //

    // TODO: would be nice to add kafka topic name semantic validation
    public void addBannedWordToTopic(String topic, String word) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, word, word);
        producer.send(record);
        producer.flush();
    }

    public void removeBannedWordFromTopic(String topic, String word) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, word, null);
        producer.send(record);
        producer.flush();
    }
}
