package ca.lerta.fly.views.flylogin;

import javax.annotation.security.PermitAll;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import ca.lerta.fly.security.AuthenticationController;
import ca.lerta.fly.views.MainLayout;

@PageTitle("Login to Fly.io")
@Route(value = "flylogin", layout = MainLayout.class)
@PermitAll
@AnonymousAllowed

public class FlyLoginOpenerView extends Div {

    private Button logout = new Button("Logout");
    private Button login = new Button("Login");

    public FlyLoginOpenerView() {
        addClassName("deploy-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());

        logout.addClickListener(e -> {
            new AuthenticationController().logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse());
        });
        login.addClickListener(e -> {
            try {
                UI ui = UI.getCurrent();
                new AuthenticationController().authenticate(() -> {
                    ui.access(() -> {
                        // reload completely to trigger the Spring Security filter chain.
                        ui.getPage().setLocation("/");
                    });
                });
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    private Component createTitle() {
        Div div = new Div();
        Paragraph p = new Paragraph(
                "Use the Connect button to go to the fly.io login page. If you do not have an account, you will be able to create one there");
        Paragraph p2 = new Paragraph("A new browser tab will open. After you have logged in, come back to this tab.");
        div.add(/* title, */ p, p2);
        return div;
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(login);
        buttonLayout.add(logout);
        return buttonLayout;
    }

}
