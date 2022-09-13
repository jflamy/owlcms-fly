package ca.lerta.fly.views.flylogin;

import javax.annotation.security.PermitAll;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import ca.lerta.fly.data.entity.SampleAddress;
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

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private Binder<SampleAddress> binder = new Binder<>(SampleAddress.class);

    public FlyOpenerView(SampleAddressService addressService) {
        addClassName("deploy-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());

        // binder.bindInstanceFields(this);

        clearForm();

        cancel.addClickListener(e -> {
            new AuthenticationController().logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse());
        });
        save.addClickListener(e -> {
            new AuthenticationController().authenticate("user", "user",
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    VaadinServletResponse.getCurrent().getHttpServletResponse());
            addressService.update(binder.getBean());
            Notification.show(binder.getBean().getClass().getSimpleName() + " stored.");
            MainLayout.getCurrent().recomputeDrawer();
            UI.getCurrent().navigate(AppsView.class);
            clearForm();
        });
    }

    private Component createTitle() {
        return new H3("Connect to Fly.io");
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

    private void clearForm() {
        this.binder.setBean(new SampleAddress());
    }

}
