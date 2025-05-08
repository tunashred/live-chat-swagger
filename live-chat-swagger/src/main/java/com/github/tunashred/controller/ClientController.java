package com.github.tunashred.controller;

import com.github.tunashred.clients.Consumer;
import com.github.tunashred.clients.Producer;
import com.github.tunashred.dto.client.ConsumerParams;
import com.github.tunashred.dto.client.ProducerParams;
import com.github.tunashred.dtos.UserMessage;
import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import javax.xml.bind.ValidationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientController {
    static String SEPARATOR = "#";

    static Map<String, Consumer> consumersMap = new HashMap<>();
    static Map<String, Producer> producersMap = new HashMap<>();

    public static void registerRoutes(Javalin app) {
        app.post("/client/produce", ctx -> {
            ProducerParams message = getProducerParams(ctx);
            sendMessage(message.getChannel(), message.getUsername(), message.getMessage());
            ctx.status(201).result("Produced: " + message);
        });

        app.post("/client/consume", ctx -> {
            ConsumerParams params = getConsumerParams(ctx);
            List<UserMessage> messages = consumeMessages(params.getChannel(), params.getUsername());
            if (messages.isEmpty()) {
                ctx.status(204).result(" ");
            } else {
                ctx.status(200).result(messages.stream().map(Object::toString).collect(Collectors.joining("\n")));
            }
        });
    }

    private static ProducerParams getProducerParams(Context ctx) throws ValidationException {
        log.trace("Querying for producer params");
        String channel = ctx.queryParam("channel");
        String username = ctx.queryParam("username");
        String message = ctx.queryParam("message");

        if (channel == null || channel.trim().isEmpty()) {
            log.error("Field 'channel' is empty");
            throw new ValidationException("Parameter 'channel' is required");
        }

        if (username == null || username.trim().isEmpty()) {
            log.error("Field 'username' is empty");
            throw new ValidationException("Parameter 'username' is required");
        }

        if (message == null || message.trim().isEmpty()) {
            log.error("Field 'message' is empty");
            throw new ValidationException("Parameter 'message' is required");
        }

        return new ProducerParams(channel, username, message);
    }

    private static void sendMessage(String channel, String username, String message) {
        Producer producer = producersMap.computeIfAbsent(channel, v -> {
            log.info("Sending message '" + message + "' to channel '" + channel + "' from user '" + username + "'");
            try {
                Properties producerProps = new Properties();
                try (InputStream inputStream = new FileInputStream("src/main/resources/client/producer.properties")) {
                    producerProps.load(inputStream);
                } catch (IOException e) {
                    throw new IOException(e);
                }

                return new Producer(producerProps);
            } catch (IOException e) {
                log.error("Unable to create new producer");
                return null;
            }
        });
        if (producer == null) {
            log.error("Producer is null");
            return;
        }
        producer.sendMessage(channel, username, message);
    }

    private static ConsumerParams getConsumerParams(Context ctx) throws ValidationException {
        log.trace("Querying for consumer params");
        String channel = ctx.queryParam("channel");
        String username = ctx.queryParam("username");

        if (channel == null || channel.trim().isEmpty()) {
            log.error("Field 'channel' is empty");
            throw new ValidationException("Parameter 'channel' is required");
        }

        if (username == null || username.trim().isEmpty()) {
            log.error("Field 'username' is empty");
            throw new ValidationException("Parameter 'username' is required");
        }

        return new ConsumerParams(channel, username);
    }

    private static List<UserMessage> consumeMessages(String channel, String username) {
        Consumer consumer = consumersMap.computeIfAbsent(channel + SEPARATOR + username, v -> {
            log.info("User '" + username + "' consuming messages from channel '" + channel + "'");
            try {
                Properties consumerProps = new Properties();
                try (InputStream inputStream = new FileInputStream("src/main/resources/client/consumer.properties")) {
                    consumerProps.load(inputStream);
                } catch (IOException e) {
                    throw new IOException(e);
                }
                return new Consumer(channel, username, consumerProps);
            } catch (IOException e) {
                log.error("Unable to create consumer for new user '" + username + "' at channel '" + channel + "'");
                return null;
            }
        });
        if (consumer == null) {
            log.error("Consumer is null");
            return List.of();
        }
        return consumer.consume();
    }

    public static void close() {
        closeConsumers();
        closeProducers();
    }

    private static void closeConsumers() {
        for (Map.Entry<String, Consumer> entry : consumersMap.entrySet()) {
            log.trace("Closing consumer '" + entry.getKey() + "'");
            entry.getValue().close();
        }
    }

    private static void closeProducers() {
        for (Map.Entry<String, Producer> entry : producersMap.entrySet()) {
            log.trace("Closing producer '" + entry.getKey() + "'");
            entry.getValue().close();
        }
    }
}
