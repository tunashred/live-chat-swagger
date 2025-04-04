package com.github.tunashred.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageRequest {
    private final String message;
    private final int dummyField;
}
