package com.github.tunashred.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tunashred.dtos.MessageInfo;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

public class ClientConsumer implements Runnable {
    private final String user;
    private final Object lock = new Object();
    private final Object doneLock = new Object();

    private KafkaConsumer<String, String> kafkaConsumer;
    private String groupTopic;
    private long offset;
    private int limit;
    private List<MessageInfo> fetchedMessages;

    @Setter
    private Consumer<ConsumerRecord<String, String>> messageHandler = message -> System.out.println(message.value());
    private AtomicBoolean keepRunnning = new AtomicBoolean(true);
    private AtomicBoolean consumeMessages = new AtomicBoolean(false);

    public ClientConsumer(String user, String groupTopic) {
        this.user = user;
        this.groupTopic = groupTopic;
        this.fetchedMessages = new ArrayList<>();
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
                this.kafkaConsumer = consumer;

                while (keepRunnning.get()) {
                    synchronized (lock) {
                        while (!consumeMessages.get()) {
                            try {
                                lock.wait();
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }

                    int count = 0;
                    ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));
                    for (var record : consumerRecords) {
                        try {
                            MessageInfo messageInfo = MessageInfo.deserialize(record.value());
                            fetchedMessages.add(messageInfo);

                            // TODO: do we need to track the offsets?

                            count++;

                            String message = messageInfo.getUser().getName() + ": " + messageInfo.getMessage();
                            System.out.println(message);
                            messageHandler.accept(record);

                            if (count >= limit) {
                                break;
                            }
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    synchronized (doneLock) {
                        consumeMessages.set(false);
                        doneLock.notify();
                    }
                }
            }
        }
    }

    public List<MessageInfo> getMessages(String groupId, long offset, int limit) {
        synchronized (lock) {
            this.groupTopic = groupId;
            this.limit = limit;
            setupTopic(offset);
            consumeMessages.set(true);
            lock.notify();
        }

        // actually this wont work since its running on the same thread lol
        synchronized (doneLock) {
            while (consumeMessages.get()) {
                try {
                    doneLock.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return fetchedMessages;
    }

    public void setupTopic(long offset) {
        List<MessageInfo> messages = new ArrayList<>();

        // gotta seek to beginning for all partitions
        if (offset < 0) {
            List<TopicPartition> partitions = this.kafkaConsumer.partitionsFor(groupTopic)
                    .stream()
                    .map(partitionInfo -> new TopicPartition(groupTopic, partitionInfo.partition()))
                    .toList();

            this.kafkaConsumer.assign(partitions);
            this.kafkaConsumer.seekToBeginning(partitions);
        } else {
            this.kafkaConsumer.subscribe(Collections.singletonList(groupTopic));
        }
    }


}
