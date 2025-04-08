package com.github.tunashred.controller;

import com.github.tunashred.clients.Consumer;
import com.github.tunashred.clients.Producer;
import com.github.tunashred.dto.BannedWordRequest;
import com.github.tunashred.dto.ConsumerParams;
import com.github.tunashred.dto.ProducerParams;
import com.github.tunashred.dtos.UserMessage;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageController {
    private static final Logger logger = LogManager.getLogger(MessageController.class);

    private static Map<String, Consumer> consumersMap = new HashMap<>();
    private static Map<String, Producer> producersMap = new HashMap<>();

//    private static Producer adminProducer = new Producer();

    public static void registerRoutes(Javalin app) {
        app.post("/client/produce", ctx -> {
            ProducerParams message = getProducerParams(ctx);
            sendMessage(message.getChannel(), message.getUsername(), message.getMessage());
            ctx.result("Produced: " + message);
        });

        app.post("/client/consume", ctx -> {
            ConsumerParams params = getConsumerParams(ctx);
            List<UserMessage> messages = consumeMessages(params.getChannel(), params.getUsername());
            if (messages.isEmpty()) {
                ctx.result("[WARNING] No records to fetch");
            } else {
                ctx.result(messages.stream().map(Object::toString).collect(Collectors.joining("\n")));
            }
        });

        app.post("admin-add-banned-word", ctx -> {
//            BannedWordRequest request = getBannedWord(ctx);
//            producerService.addBannedWordToTopic(request.getTopic(), request.getWord());
//            ctx.result("Added banned word: " + request.getWord());
        });

        app.post("admin-remove-banned-word", ctx -> {
//            BannedWordRequest request = getBannedWord(ctx);
//            producerService.removeBannedWordFromTopic(request.getTopic(), request.getWord());
//            ctx.result("Added banned word: " + request.getWord());
        });

        app.get("/", ctx -> ctx.redirect("swagger-ui.html"));
    }

    private static ProducerParams getProducerParams(Context ctx) throws ValidationException {
        String channel = ctx.queryParam("channel");
        String username = ctx.queryParam("username");
        String message = ctx.queryParam("message");

        if (channel == null || channel.trim().isEmpty()) {
            throw new ValidationException("Parameter 'channel' is required");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Parameter 'username' is required");
        }

        if (message == null || message.trim().isEmpty()) {
            throw new ValidationException("Parameter 'message' is required");
        }

        return new ProducerParams(channel, username, message);
    }

    private static void sendMessage(String channel, String username, String message) {
        Producer producer = producersMap.computeIfAbsent(channel, v -> {
            try {
                return new Producer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        producer.sendMessage(channel, username, message);
    }

    private static ConsumerParams getConsumerParams(Context ctx) throws ValidationException {
        String channel = ctx.queryParam("channel");
        String username = ctx.queryParam("username");

        if (channel == null || channel.trim().isEmpty()) {
            throw new ValidationException("Parameter 'channel' is required");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Parameter 'username' is required");
        }

        return new ConsumerParams(channel, username);
    }

    private static List<UserMessage> consumeMessages(String channel, String username) {
        Consumer consumer = consumersMap.computeIfAbsent(channel + "-" + username, v -> {
            try {
                return new Consumer(channel, username);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return consumer.consume();
    }

    private static BannedWordRequest getBannedWord(Context ctx) throws ValidationException {
        String topic = ctx.queryParam("topic");
        String word = ctx.queryParam("word");

        if (topic == null || topic.trim().isEmpty()) {
            throw new ValidationException("Parameter 'topic' is required");
        }

        if (word == null || word.trim().isEmpty()) {
            throw new ValidationException("Parameter 'word' is required");
        }
        return new BannedWordRequest(topic, word);
    }

//    private static void addBannedWordToTopic(String topic, String word) {
//        ProducerRecord<String, String> record = new ProducerRecord<>(topic, word, word);
//        adminProducer.send(record);
//        adminProducer.flush();
//    }
//
//    private static void removeBannedWordFromTopic(String topic, String word) {
//        ProducerRecord<String, String> record = new ProducerRecord<>(topic, word, null);
//        adminProducer.send(record);
//        adminProducer.flush();
//    }
}
