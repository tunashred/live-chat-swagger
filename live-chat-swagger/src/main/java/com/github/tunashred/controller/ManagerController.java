package com.github.tunashred.controller;

import com.github.tunashred.dto.manager.DoubleStringParams;
import com.github.tunashred.dto.manager.FileTopicParams;
import com.github.tunashred.manager.Manager;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ManagerController {
    static Manager manager;

    static {
        try {
            manager = new Manager();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerRoutes(Javalin app) {
        app.get("/manager/list-packs", ctx -> {
            ctx.result(String.join("\n", Manager.listPacks()));
        });

        app.post("/manager/create-pack", ctx -> {
            FileTopicParams params = getFileTopicParams(ctx);
            Boolean success = Manager.createPackFromFile(params.getFilePath(), params.getTopic());
            if (success) {
                ctx.status(200).result("Pack created successfully.");
            } else {
                ctx.status(400).result("Failed to create pack.");
            }
        });

        app.post("/manager/add-word", ctx -> {
            DoubleStringParams params = getDoubleStringParams(ctx);
            System.out.println(params.getStringFirst());
            System.out.println(params.getStringSecond());
            Boolean success = Manager.addWord(params.getStringFirst(), params.getStringSecond());
            if (success) {
                ctx.status(200).result("Word added successfully.");
            } else {
                ctx.status(400).result("Failed add word.");
            }
        });

        app.post("/manager/add-words", ctx -> {
            FileTopicParams params = getFileTopicParams(ctx);
            Boolean success = Manager.addWords(params.getFilePath(), params.getTopic());
            if (success) {
                ctx.status(200).result("Words added successfully.");
            } else {
                ctx.status(400).result("Failed add words.");
            }
        });

        app.post("/manager/delete-word", ctx -> {
            DoubleStringParams params = getDoubleStringParams(ctx);
            Boolean success = Manager.deleteWord(params.getStringFirst(), params.getStringSecond());
            if (success) {
                ctx.status(200).result("Word removed successfully.");
            } else {
                ctx.status(400).result("Failed remove word.");
            }
        });

        app.post("/manager/delete-pack", ctx -> {
            String param = getStringParam(ctx);
            Boolean success = Manager.deletePack(param);
            if (success) {
                ctx.status(200).result("Words added successfully.");
            } else {
                ctx.status(400).result("Failed add words.");
            }
        });

        app.get("/manager/search-word", ctx -> {
            String param = getStringParam(ctx);
            ctx.result(String.join("\n", Manager.searchWord(param)));
        });

        app.get("/manager/get-pack", ctx -> {
            String param = getStringParam(ctx);
            ctx.result(String.join("\n", Manager.getPack(param)));
        });
    }

    private static FileTopicParams getFileTopicParams(Context ctx) throws ValidationException, IOException {
        log.trace("Querying for file and topic params");
        UploadedFile file = ctx.uploadedFile("file");
        String topic = ctx.queryParam("topic");

        if (file != null) {
            Path target = Paths.get("uploads", file.filename());
            Files.createDirectories(target.getParent());
            Files.copy(file.content(), target, StandardCopyOption.REPLACE_EXISTING);
            log.trace("File uploaded");
        } else {
            throw new ValidationException("Parameter 'file' is required");
        }

        if (topic == null || topic.trim().isEmpty()) {
            log.error("Field 'topic' is empty");
            throw new ValidationException("Parameter 'topic' is required");
        }
        return new FileTopicParams(file.filename(), topic);
    }

    private static DoubleStringParams getDoubleStringParams(Context ctx) throws ValidationException {
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
        return new DoubleStringParams(pack, word);
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
