package com.github.tunashred.kafka;

import com.github.tunashred.dtos.GroupChat;
import com.github.tunashred.dtos.MessageInfo;
import com.github.tunashred.dtos.User;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientProducer {
    private final KafkaProducer<String, String> producer;
    private final User user;
    private final GroupChat groupChat;
    private AtomicBoolean keepRunnning = new AtomicBoolean(true);
    private MessageInfo message;

    public ClientProducer(User user, GroupChat groupChat) {
        this.groupChat = groupChat;
        this.user = user;

        Properties producerProps = new Properties();
        try (InputStream propsFile = new FileInputStream("src/main/resources/producer.properties")) {
            producerProps.load(propsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.producer = new KafkaProducer<>(producerProps);
    }

    public void sendMessage(MessageInfo messageInfo) {
        this.message = messageInfo;
        try {
            String serialized = MessageInfo.serialize(message);
            ProducerRecord<String, String> record = new ProducerRecord<>("unsafe_chat", message.getGroupChat().getChatID(), serialized);

            producer.send(record);
            producer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
