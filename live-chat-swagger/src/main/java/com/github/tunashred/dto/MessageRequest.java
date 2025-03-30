package com.github.tunashred.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {
    private String groupName;
    private String userName;
    private String messageContent;
}
