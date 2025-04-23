package com.github.tunashred.dto.manager;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoubleStringParams {
    String stringFirst;
    String stringSecond;
}
