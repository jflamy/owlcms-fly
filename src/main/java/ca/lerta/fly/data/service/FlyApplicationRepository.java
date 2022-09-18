package ca.lerta.fly.data.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.lerta.fly.Application;
import ca.lerta.fly.data.entity.FlyApplication;
import ca.lerta.fly.inmemory.InMemoryJpaRepository;
import ca.lerta.fly.inmemory.UUIDPrimaryKeyGenerator;
import ch.qos.logback.classic.Logger;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;

@Component
@Primary
public class FlyApplicationRepository extends InMemoryJpaRepository<FlyApplication, UUID> {

    private static final String LABEL_PREFIX = "LABEL_";
    private static final String BUNDLE_PREFIX = "BUNDLE_";
    ObjectMapper mapper = new ObjectMapper();
    private Logger logger = (Logger) LoggerFactory.getLogger(FlyApplicationRepository.class);

    public FlyApplicationRepository() {
        this.setPrimaryKeyGenerator(new UUIDPrimaryKeyGenerator());
    }

    public void loadRepository(String accessToken) {
        this.deleteAll();
        String json = appListGetJson(accessToken);
        List<JsonNode> appData = appListFilterJson(json);
        for (JsonNode node : appData) {
            Map<String, Object> result = mapper.convertValue(node, new TypeReference<Map<String, Object>>() {
            });
            String appName = (String) result.get("name");
            String image = "";
            String bundleName = "";
            String label = "";

            String releasesJson = appReleasesGetJson(accessToken, appName);
            List<JsonNode> appReleasesData = appReleasesFilterJson(releasesJson);
            for (JsonNode releaseNode : appReleasesData) {
                Map<String, Object> result2 = mapper.convertValue(releaseNode,
                        new TypeReference<Map<String, Object>>() {
                        });
                image = (String) result2.get("image");
            }

            String secretsJson = appSecretsGetJson(accessToken, appName);
            List<JsonNode> appSecretsData = appSecretsFilterJson(secretsJson);
            for (JsonNode secretNode : appSecretsData) {
                Map<String, Object> result3 = mapper.convertValue(secretNode, new TypeReference<Map<String, Object>>() {
                });
                logger.warn("**** {}",result3);
                String secretName = (String) result3.get("secret");
                if (secretName != null) {
                    if (secretName.startsWith(BUNDLE_PREFIX)) {
                        bundleName = secretName.substring(BUNDLE_PREFIX.length());
                    } else if (secretName.startsWith(LABEL_PREFIX)) {
                        label = secretName.substring(LABEL_PREFIX.length());
                    }
                }
            }

            String appStatus = (String) result.get("status");
            logger.info("name {}, status {}, label {}, bundle {}, image {}", appName, appStatus, label, bundleName,
                    image);
            FlyApplication fa = new FlyApplication();
            fa.setName((String) appName);
            fa.setRunning(appStatus.contentEquals("running"));
            fa.setLabel(label);
            fa.setBundle(bundleName);
            this.save(fa);
        }
    }

    private List<JsonNode> appListFilterJson(String json) {
        try {
            Scope rootScope = Application.rootJqScope;
            JsonQuery q = JsonQuery.compile(
                    ".[] | select(.Organization.Slug == \"personal\") | {name: .Name, status: .Status, org: .Organization.Slug }",
                    Versions.JQ_1_6);
            JsonNode in = mapper.readTree(json);
            final List<JsonNode> out = new ArrayList<>();
            q.apply(Scope.newChildScope(rootScope), in, out::add);
            System.out.println(out);
            return out;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String appListGetJson(String accessToken) {
        var processBuilder = new ProcessBuilder();
        String json = "";
        processBuilder.command("flyctl", "apps", "list", "-j", "-t", accessToken);
        try {
            var process = processBuilder.start();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                json = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            // ignored
        }
        return json;
    }

    private String appReleasesGetJson(String accessToken, String appName) {
        var processBuilder = new ProcessBuilder();
        String json = "";
        processBuilder.command("flyctl", "releases", "--app", appName, "-j", "-t", accessToken);
        try {
            var process = processBuilder.start();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                json = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            // ignored
        }
        return json;
    }

    private List<JsonNode> appReleasesFilterJson(String json) {
        try {
            Scope rootScope = Application.rootJqScope;
            JsonQuery q = JsonQuery.compile("{image: .[1].ImageRef}", Versions.JQ_1_6);
            JsonNode in = mapper.readTree(json);
            final List<JsonNode> out = new ArrayList<>();
            q.apply(Scope.newChildScope(rootScope), in, out::add);
            System.out.println(out);
            return out;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String appSecretsGetJson(String accessToken, String appName) {
        var processBuilder = new ProcessBuilder();
        String json = "";
        processBuilder.command("flyctl", "secrets", "list", "--app", appName, "-j", "-t", accessToken);
        try {
            var process = processBuilder.start();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                json = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            // ignored
        }
        return json;
    }

    private List<JsonNode> appSecretsFilterJson(String json) {
        try {
            Scope rootScope = Application.rootJqScope;
            JsonQuery q = JsonQuery.compile(".[] | {secret: .Name}", Versions.JQ_1_6);
            JsonNode in = mapper.readTree(json);
            final List<JsonNode> out = new ArrayList<>();
            q.apply(Scope.newChildScope(rootScope), in, out::add);
            System.out.println(out);
            return out;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
