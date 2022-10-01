package ca.lerta.fly.views.flylogin;

import javax.annotation.security.PermitAll;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import ca.lerta.fly.security.AuthenticationController;
import ca.lerta.fly.views.MainLayout;

@PageTitle("Owlcms Application Hosting on Fly.io")
@Route(value = "flylogin", layout = MainLayout.class)
@PermitAll
@AnonymousAllowed

public class FlyLoginOpenerView extends Div {

    private Button logout = new Button("Logout");
    private Button login = new Button("Login to existing account");
    private Button newAccount = new Button("Create new Account");

    public FlyLoginOpenerView() {
        addClassName("deploy-view");
        add(createLoginTitle());
        add(createNewAccountTitle());
        add(createLogoutTitle());

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
                }, false);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        newAccount.addClickListener(e -> {
            try {
                UI ui = UI.getCurrent();
                new AuthenticationController().authenticate(() -> {
                    ui.access(() -> {
                        // reload completely to trigger the Spring Security filter chain.
                        ui.getPage().setLocation("/");
                    });
                }, true);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        newAccount.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
    }

    private Component createNewAccountTitle() {
        Div div = new Div();
        Paragraph p0 = new Paragraph("\u00a0");
        Paragraph p1 = new Paragraph("If you do NOT have a fly.io account, use the New Account button to create one.");
        Paragraph p2 = new Paragraph("A new browser tab will open to create the account.");
        Paragraph p3 = new Paragraph("PLEASE MAKE SURE THAT");
        UnorderedList ul = new UnorderedList();
        ul.add(
            new ListItem("you have associated a credit card or added credit to your account"),
            new ListItem("that you have received a confirmation e-mail and confirmed your e-mail address"));
        Paragraph p4 = new Paragraph("After confiming your e-mail, you can come back to this tab and create your applications");
        div.add(p0, p1, p2, p3, ul, p4, newAccount);
        return div;
    }

    private Component createLoginTitle() {
        Div div = new Div();
        Paragraph p0 = new Paragraph("\u00a0");
        Paragraph p1 = new Paragraph(
                "If you already have an account, use the Login button to go to the fly.io login page.");
        Paragraph p2 = new Paragraph("A new browser tab will open. After you have logged in, come back to this tab.");
        div.add(p0, p1, p2, login);
        return div;
    }

    private Component createLogoutTitle() {
        Div div = new Div();
        Paragraph p0 = new Paragraph("\u00a0");
        Paragraph p1 = new Paragraph(
                "Use the logout button to disconnect from fly.io (or to switch accounts).");
        div.add(p0, p1, logout);
        return div;
    }
}
