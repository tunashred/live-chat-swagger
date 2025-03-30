package com.github.tunashred.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tunashred.dtos.MessageInfo;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

public class ClientConsumer implements Runnable {
    private final String user;
    private final String groupTopic;
    @Setter
    private Consumer<ConsumerRecord<String, String>> messageHandler = message -> System.out.println(message.value());
    private AtomicBoolean keepRunnning = new AtomicBoolean(true);

    public ClientConsumer(String user, String groupTopic) {
        this.user = user;
        this.groupTopic = groupTopic;
    }

    public void stopRunning() {
        keepRunnning.set(false);
    }

    @Override
    public void run() throws RuntimeException {
        System.out.println(this.user + " " + this.groupTopic);
        Properties consumerProps = new Properties();
        try (InputStream propsFile = new FileInputStream("src/main/resources/consumer.properties")) {
            consumerProps.load(propsFile);
            consumerProps.put(GROUP_ID_CONFIG, "consumer-" + user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (keepRunnning.get()) {
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
                consumer.subscribe(Collections.singletonList(groupTopic));

                while (keepRunnning.get()) {
                    ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));
                    for (var record : consumerRecords) {
                        try {
                            MessageInfo messageInfo = MessageInfo.deserialize(record.value());

                            String message = messageInfo.getUser().getName() + ": " + messageInfo.getMessage();
                            System.out.println(message);
                            messageHandler.accept(record);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    consumer.commitSync();
                }
            }
        }
    }
}
