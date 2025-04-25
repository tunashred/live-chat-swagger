package com.github.tunashred.controller;

import com.github.tunashred.dto.manager.FileTopicParams;
import com.github.tunashred.dto.manager.TopicWordParams;
import com.github.tunashred.manager.Manager;
import com.github.tunashred.streamer.Streamer;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;

@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ManagerController {
    static Manager manager;
    static String PREFERENCES_TOPIC = "streamer-preferences";
    static String STREAMER_CONSUMER_PROPERTIES = "src/main/resources/streamer/consumer.properties";
    static String STREAMER_PRODUCER_PROPERTIES = "src/main/resources/streamer/producer.properties";
    static String STREAMER_STREAMS_PROPERTIES = "src/main/resources/streamer/streams.properties";

    public static void registerRoutes(Javalin app) {
        Properties streamsProps = new Properties();
        streamsProps.put(APPLICATION_ID_CONFIG, "swagger-manager-controller-streamer-streams");

        Properties consumerProps = new Properties();
        consumerProps.put(GROUP_ID_CONFIG, "swagger-manager-controller-streamer-consumer");
        Streamer streamer = new Streamer(PREFERENCES_TOPIC, STREAMER_CONSUMER_PROPERTIES, STREAMER_PRODUCER_PROPERTIES,
                STREAMER_STREAMS_PROPERTIES, consumerProps, streamsProps);

        manager = new Manager(streamer);
        streamer.start();

        app.get("/manager/list-packs", ctx -> {
            List<String> packs = Manager.listPacks();
            if (packs.isEmpty()) {
                ctx.status(204).result("No packs available");
            } else {
                ctx.status(200).result(String.join("\n", packs));
            }
        });

        app.get("/manager/search-word", ctx -> {
            String param = getStringParam(ctx);
            List<String> matches = Manager.searchWord(param);
            if (matches.isEmpty()) {
                ctx.status(204).result("No packs contain the searched word or expression");
            } else {
                ctx.status(200).result(String.join("\n", matches));
            }
        });

        app.get("/manager/get-pack", ctx -> {
            String param = getStringParam(ctx);
            List<String> contents = Manager.getPack(param);
            if (contents.isEmpty()) {
                ctx.status(204).result("The pack does not exist or it is empty");
            } else {
                ctx.status(200).result(String.join("\n", contents));
            }
        });

        app.post("/manager/create-pack", ctx -> {
            FileTopicParams params = getFileTopicParams(ctx);
            boolean success = Manager.createPackFromFile(params.getInputStream(), params.getTopic());
            if (success) {
                ctx.status(201).result("Pack created successfully");
            } else {
                ctx.status(400).result("Failed to create pack");
            }
        });

        app.post("/manager/add-word", ctx -> {
            TopicWordParams params = getTopicWordParams(ctx);
            boolean success = Manager.addWord(params.getStringFirst(), params.getStringSecond());
            if (success) {
                ctx.status(201).result("Word added successfully");
            } else {
                ctx.status(400).result("Failed add word");
            }
        });

        app.post("/manager/add-words", ctx -> {
            FileTopicParams params = getFileTopicParams(ctx);
            boolean success = Manager.addWords(params.getInputStream(), params.getTopic());
            if (success) {
                ctx.status(201).result("Words added successfully");
            } else {
                ctx.status(400).result("Failed add words");
            }
        });

        app.post("/manager/delete-word", ctx -> {
            TopicWordParams params = getTopicWordParams(ctx);
            boolean success = Manager.deleteWord(params.getStringFirst(), params.getStringSecond());
            if (success) {
                ctx.status(200).result("Word removed successfully");
            } else {
                ctx.status(400).result("Failed remove word");
            }
        });

        app.post("/manager/delete-pack", ctx -> {
            String param = getStringParam(ctx);
            boolean success = Manager.deletePack(param);
            if (success) {
                ctx.status(200).result("Pack deleted successfully");
            } else {
                ctx.status(400).result("Failed to delete pack");
            }
        });
    }

    private static FileTopicParams getFileTopicParams(Context ctx) throws ValidationException, IOException {
        log.trace("Querying for file and topic params");
        UploadedFile file = ctx.uploadedFile("file");
        String topic = ctx.queryParam("topic");

        if (file != null) {
            log.trace("File uploaded");
        } else {
            throw new ValidationException("Parameter 'file' is required");
        }

        if (topic == null || topic.trim().isEmpty()) {
            log.error("Field 'topic' is empty");
            throw new ValidationException("Parameter 'topic' is required");
        }
        log.trace("'FileTopicParams' validated");
        return new FileTopicParams(file.content(), topic);
    }

    private static TopicWordParams getTopicWordParams(Context ctx) throws ValidationException {
        log.trace("Querying for string param");
        String pack = ctx.queryParam("pack");
        String word = ctx.queryParam("word");

        if (pack == null || pack.trim().isEmpty()) {
            log.error("Field 'pack' is empty");
            throw new ValidationException("Parameter 'pack' is required");
        }
        if (!pack.startsWith("pack-")) {
            pack = "pack-" + pack;
        }

        if (word == null || word.trim().isEmpty()) {
            log.error("Field 'word' is empty");
            throw new ValidationException("Parameter 'word' is required");
        }
        return new TopicWordParams(pack, word);
    }

    private static String getStringParam(Context ctx) throws ValidationException {
        log.trace("Querying for string param");
        String text = ctx.queryParam("text");

        if (text == null || text.trim().isEmpty()) {
            log.error("Field 'text' is empty");
            throw new ValidationException("Parameter 'text' is required");
        }
        return text;
    }

    public static void close() {
        manager.close();
    }
}
