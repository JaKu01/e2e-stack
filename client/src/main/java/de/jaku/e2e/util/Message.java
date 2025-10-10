package de.jaku.e2e.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Message {

    private final String base64EncodedMessage;

    @JsonCreator
    public Message(@JsonProperty("message") String base64EncodedMessage) {
        this.base64EncodedMessage = base64EncodedMessage;
    }

    @JsonProperty("message")
    public String getBase64EncodedMessage() {
        return base64EncodedMessage;
    }
}