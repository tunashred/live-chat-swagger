package com.github.tunashred.kafka;

import com.github.tunashred.config.KafkaConfig;
import com.github.tunashred.dtos.MessageInfo;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;

public class ClientProducer {
    private final Producer<String, String> producer;

    public ClientProducer() {
        this.producer = new KafkaProducer<>(KafkaConfig.getProducerProps());
    }

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

    public void close() {
        producer.close();
    }
}
