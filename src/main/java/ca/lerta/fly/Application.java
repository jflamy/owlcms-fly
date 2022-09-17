package ca.lerta.fly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "owlcms-fly", variant = Lumo.DARK)
@PWA(name = "owlcms-fly", shortName = "owlcms-fly", offlineResources = {})
@NpmPackage(value = "line-awesome", version = "1.3.0")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.0.6")
@Push
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {
    public static Scope rootJqScope = null;

    public static void main(String[] args) {
        initJacksonJq();
        SpringApplication.run(Application.class, args);
    }

    private static void initJacksonJq() {
        // a Scope is a container of built-in/user-defined functions and variables.
        rootJqScope = Scope.newEmptyScope();
        // BuiltinFunctionLoader loads built-in functions from the classpath.
        BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootJqScope);
    }

}
