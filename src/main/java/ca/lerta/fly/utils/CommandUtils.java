package ca.lerta.fly.utils;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.VaadinSession;

import ch.qos.logback.classic.Logger;

public class CommandUtils {

    private static final String FLYCTL_COMMANDS = "flyctlCommands";
    private static final Logger logger = (Logger) LoggerFactory.getLogger(CommandUtils.class);

    public static String[] getCommandArgs(String key, Object... arguments) {
        var flyCommands = ResourceBundle.getBundle(FLYCTL_COMMANDS);
        var stringWithSlots = flyCommands.getString(key);
        var substitutedStrings = MessageFormat.format(stringWithSlots, arguments);
        logger.warn("about to run: {}", substitutedStrings);
        return substitutedStrings.split(" +");
    }

    public static String getProcessOutput(ProcessBuilder processBuilder) {
            try {
                var process = processBuilder.start();
                var jsons = new String[1];
                var errors = new String[1];
                jsons[0] = "";
                errors[0] = "";

                // read stdout
                new Thread(() -> {
                    try (var reader = process.inputReader()) {
                        jsons[0] = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                    } catch (Exception e) {
                        // ignored
                    }
                }).start();

                // read stderr
                new Thread(() -> {
                    try (var reader = process.errorReader()) {
                        errors[0] = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                    } catch (Exception e) {
                        // ignored
                    }
                }).start();
                
                var status = process.waitFor();
                if (status != 0) {
                    throw new RuntimeException("Execution error: "+errors[0]);
                }
                return jsons[0];
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return "";   
    }

    public static String getAccessToken() {
        return (String) VaadinSession.getCurrent().getAttribute("ACCESS_TOKEN");
    }
}
