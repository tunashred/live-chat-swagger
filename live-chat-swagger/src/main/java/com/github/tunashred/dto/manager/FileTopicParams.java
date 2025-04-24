package com.github.tunashred.dto.manager;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.InputStream;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileTopicParams {
    InputStream inputStream;
    String topic;
}
