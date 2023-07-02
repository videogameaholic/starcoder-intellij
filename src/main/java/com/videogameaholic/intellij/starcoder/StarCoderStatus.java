package com.videogameaholic.intellij.starcoder;

import java.util.stream.Stream;

public enum StarCoderStatus {
    UNKNOWN(0,"Unknown"),
    OK(200,"OK"),
    BAD_REQUEST(400,"Bad request/token"),
    NOT_FOUND(404,"404 Not found"),
    TOO_MANY_REQUESTS(429,"Too many requests right now");

    private int code;
    private String displayValue;

    StarCoderStatus(int i, String s) {
        code = i;
        displayValue = s;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static StarCoderStatus getStatusByCode(int code) {
        return Stream.of(StarCoderStatus.values())
                .filter(s -> s.getCode() == code)
                .findFirst()
                .orElse(StarCoderStatus.UNKNOWN);
    }
}
