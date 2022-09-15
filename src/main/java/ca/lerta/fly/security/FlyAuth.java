package ca.lerta.fly.security;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;

import ch.qos.logback.classic.Logger;

public class FlyAuth {
    private static final String BASE_URL = "https://api.fly.io";

    private static final int MAX_SECONDS = 60;
    Logger logger = (Logger) LoggerFactory.getLogger(FlyAuth.class);
    public Integer flySessionId = null;

    String accessToken = null;

    /**
     * The openSession function opens a session, emulating the flyctl CLI.
     * Values to be used when opening the interactive browser session.
     * 
     * @return the session id
     */
    public Map<String, String> createSession() {
        java.net.InetAddress localMachine;
        try {
            localMachine = java.net.InetAddress.getLocalHost();
            Map<String, Object> apiRequest = Map.of("hostname", localMachine.getHostName(), "signup", true);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonApiRequest = objectMapper.writeValueAsString(apiRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "/api/v1/cli_sessions"))
                    .POST(BodyPublishers.ofString(jsonApiRequest))
                    .setHeader("Content-Type", "application/json")
                    .build();
            logger.info("calling {}", request.uri());
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new RuntimeException("unexpected result code " + response.statusCode());
            }
            String jsonInput = response.body();
            logger.info("response 1 {}", jsonInput);
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
            };
            Map<String, String> map = objectMapper.readValue(jsonInput, typeRef);
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void waitForTokenString(String id, Consumer<String> callback) {
        ObjectMapper objectMapper = new ObjectMapper();
        new Thread(() -> {
            int count = 0;
            while (count < MAX_SECONDS && accessToken == null) {
                try {
                    String sessionUrl = BASE_URL + "/api/v1/cli_sessions/" + id;
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI(sessionUrl))
                            .GET()
                            .setHeader("Content-Type", "application/json")
                            .build();
                    logger.info("calling {}", request.uri());
                    HttpClient client = HttpClient.newHttpClient();
                    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        String jsonInput = response.body();
                        logger.info("response 2 {}", jsonInput);
                        TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
                        };
                        Map<String, String> map = objectMapper.readValue(jsonInput, typeRef);
                        accessToken = map.get("access_token");
                        callback.accept(accessToken);
                    } else {
                        Thread.sleep(1000);
                    }
                    count++;
                } catch (Exception e) {
                    logger.error("exception in token retrieval", e);
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public void openFlyLogin(String authUrl, UI ui) {
        logger.info("opening login page {}", authUrl);
        new Thread(() -> {
          ui.access(() -> {
            ui.getPage().open(authUrl);
          });
        }).start();
        return;
      }

}
