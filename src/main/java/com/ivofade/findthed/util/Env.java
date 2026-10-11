package com.ivofade.findthed.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class Env {

    private static final Map<String, String> VALUES = new HashMap<>();

    static {
        Path file = Path.of(".env");
        if (Files.exists(file)) {
            try {
                List<String> lines = Files.readAllLines(file);
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    int equalSign = line.indexOf('=');
                    if(equalSign <= 0) continue;

                    String key = line.substring(0, equalSign).trim();
                    String value = line.substring(equalSign + 1).trim();
                        if (value.length() >= 2
                                && ((value.startsWith("\"") && value.endsWith("\""))
                                || (value.startsWith("'") && value.endsWith("'")))) {
                            value = value.substring(1, value.length() - 1);
                    }

                    VALUES.put(key, value);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not read .env file", e);
            }
        }
    }

    private Env() {}

    public static String get(String key) {
        String value = VALUES.get(key);
        return value != null ? value : System.getenv(key);
    }

    public static String require(String key) {
        String value = get(key);
        if(value == null) {
            throw new IllegalStateException(
                "Missing config: " + key + " (check .env and the program's working directory)"
            );
        }
        return value;
    }

}
