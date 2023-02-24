package org.example;

import org.example.config.Config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) throws IOException {

        Config config = new Config(
                true,
                "your-language-id",
                "path-of-your-service-account-json-key",
                "your-agent-location",
                "your-environment-id",
                "your-agent-id"
        );

        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        DetectIntentStreamBased.detectIntent(
                config.getProjectId(),
                config.getLocationId(),
                config.getAgentId(),
                "session_text_" + LocalDateTime.now().format(myFormatObj),
                config.getEnvironmentId(),
                config.getLanguageId(),
                config.getPathKey()
        );
    }
}