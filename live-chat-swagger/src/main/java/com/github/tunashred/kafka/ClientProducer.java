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

public class ClientProducer implements Runnable {
    private final User user;
    private final GroupChat groupChat;
    private final Object lock = new Object();
    private AtomicBoolean keepRunnning = new AtomicBoolean(true);
    private AtomicBoolean hasMessage = new AtomicBoolean(false);
    private MessageInfo message;

    public ClientProducer(User user, GroupChat groupChat) {
        this.groupChat = groupChat;
        this.user = user;
    }

    @Override
    public void run() throws RuntimeException {
        Properties producerProps = new Properties();
        try (InputStream propsFile = new FileInputStream("src/main/resources/producer.properties")) {
            producerProps.load(propsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (keepRunnning.get()) {
            try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
                while (keepRunnning.get()) {
                    // what if what I am doing here is just the kafka streams boilerplate?
                    // if so, it would be worth to talk about this in my thesis to show the difference between KafkaProducer
                    // and KStream implementations
                    synchronized (lock) {
                        while (!hasMessage.get()) {
                            try {
                                lock.wait();
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                    try {
                        String serialized = MessageInfo.serialize(message);
                        ProducerRecord<String, String> record = new ProducerRecord<>("unsafe_chat", message.getGroupChat().getChatID(), serialized);

                        producer.send(record);
                        hasMessage.set(false);
                        producer.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public void sendMessage(MessageInfo messageInfo) {
        synchronized (lock) {
            this.message = messageInfo;
            hasMessage.set(true);
            lock.notify();
        }
    }
}
