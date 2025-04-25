package com.github.tunashred.controller;

import com.github.tunashred.dto.streamer.StreamerPackParams;
import com.github.tunashred.streamer.Streamer;
import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.extern.log4j.Log4j2;

import javax.xml.bind.ValidationException;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;

@Log4j2
public class StreamerController {
    static Streamer streamer;
    static String PREFERENCES_TOPIC = "streamer-preferences";
    static String STREAMER_CONSUMER_PROPERTIES = "src/main/resources/streamer/consumer.properties";
    static String STREAMER_PRODUCER_PROPERTIES = "src/main/resources/streamer/producer.properties";
    static String STREAMER_STREAMS_PROPERTIES = "src/main/resources/streamer/streams.properties";

    public static void registerRoutes(Javalin app) {
        Properties streamsProps = new Properties();
        streamsProps.put(APPLICATION_ID_CONFIG, "swagger-streamer-streams-controller");

        Properties consumerProps = new Properties();
        consumerProps.put(GROUP_ID_CONFIG, "swagger-streamer-consumer-controller");
        streamer = new Streamer(PREFERENCES_TOPIC, STREAMER_CONSUMER_PROPERTIES, STREAMER_PRODUCER_PROPERTIES,
                STREAMER_STREAMS_PROPERTIES, consumerProps, streamsProps);

        streamer.start();

        app.get("/streamer/list-packs", ctx -> {
            List<String> packs = Streamer.listPacks();
            if (packs.isEmpty()) {
                ctx.status(204).result("There are no available packs");
            } else {
                ctx.status(200).result(String.join("\n", packs));
            }
        });

        app.post("/streamer/add-pack", ctx -> {
            StreamerPackParams params = getStreamerPackParams(ctx);
            boolean success = Streamer.addPreference(params.getStreamer(), params.getPack());
            if (success) {
                ctx.status(201).result("Pack added succesfully");
            } else {
                ctx.status(400).result("Failed to add pack");
            }
        });

        app.post("/streamer/remove-pack", ctx -> {
            StreamerPackParams params = getStreamerPackParams(ctx);
            boolean success = Streamer.removePreference(params.getStreamer(), params.getPack());
            if (success) {
                ctx.status(200).result("Pack removed succesfully");
            } else {
                ctx.status(400).result("Failed to remove pack");
            }
        });
    }

    private static StreamerPackParams getStreamerPackParams(Context ctx) throws ValidationException {
        log.trace("Querying for streamer and pack params");
        String streamer = ctx.queryParam("streamer");
        String pack = ctx.queryParam("pack");

        if (streamer == null || streamer.trim().isEmpty()) {
            log.error("Field 'streamer' is empty");
            throw new ValidationException("Parameter 'word' is required");
        }

        if (pack == null || pack.trim().isEmpty()) {
            log.error("Field 'pack' is empty");
            throw new ValidationException("Parameter 'pack' is required");
        }
        if (!pack.startsWith("pack-")) {
            pack = "pack-" + pack;
        }

        return new StreamerPackParams(streamer, pack);
    }

    public static void close() {
        streamer.close();
    }
}
