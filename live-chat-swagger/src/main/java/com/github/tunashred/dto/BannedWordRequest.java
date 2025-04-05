package com.github.tunashred.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BannedWordRequest {
    private final String topic;
    private final String word;
}
