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
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import ca.lerta.fly.data.service.SampleAddressService;
import ca.lerta.fly.security.AuthenticationController;
import ca.lerta.fly.views.MainLayout;
import ca.lerta.fly.views.apps.AppsView;

@PageTitle("Login to Fly.io")
@Route(value = "flylogin", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
@AnonymousAllowed

public class FlyOpenerView extends Div {

    private Button cancel = new Button("Logout");
    private Button save = new Button("Login");

    public FlyOpenerView(SampleAddressService addressService) {
        addClassName("deploy-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());

        cancel.addClickListener(e -> {
            new AuthenticationController().logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse());
        });
        save.addClickListener(e -> {
            new AuthenticationController().authenticate("user", "user",
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse());
            //Notification.show(binder.getBean().getClass().getSimpleName() + " stored.");
            UI.getCurrent().navigate(AppsView.class);
            MainLayout.getCurrent().recomputeDrawer();
        });
    }

    private Component createTitle() {
        Div div = new Div();
        //H3 title = new H3("Connect to Fly.io");
        Paragraph p = new Paragraph("Use the Connect button to go to the fly.io login page. If you do not have an account, you will be able to create one there");
        Paragraph p2 = new Paragraph("A new browser tab will open. After you have logged in, come back to this tab.");
        div.add(/*title,*/ p, p2);
        return div;
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        buttonLayout.add(cancel);
        return buttonLayout;
    }

}
