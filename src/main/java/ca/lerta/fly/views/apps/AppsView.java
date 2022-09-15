package ca.lerta.fly.views.apps;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.PermitAll;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import ca.lerta.fly.data.entity.Application;
import ca.lerta.fly.data.service.ApplicationService;
import ca.lerta.fly.views.MainLayout;
import ch.qos.logback.classic.Logger;

@PageTitle("Fly.io owlcms Applications")
@Route(value = "apps/:applicationID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)

public class AppsView extends Div implements BeforeEnterObserver {
    Logger logger = (Logger) LoggerFactory.getLogger(AppsView.class);

    private final String APPLICATION_ID = "applicationID";
    private final String APPLICATION_EDIT_ROUTE_TEMPLATE = "apps/%s/edit";

    private Grid<Application> grid = new Grid<>(Application.class, false);

    private TextField name;
    private Checkbox nameOn;
    private TextField results;
    private Checkbox resultsOn;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Application> binder;

    private Application application;

    private final ApplicationService applicationService;

    @Autowired
    public AppsView(ApplicationService applicationService) {
        this.applicationService = applicationService;
        addClassNames("apps-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        LitRenderer<Application> nameOnRenderer = LitRenderer.<Application>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", nameOn -> nameOn.isNameOn() ? "check" : "minus").withProperty("color",
                        nameOn -> nameOn.isNameOn()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(nameOnRenderer).setHeader("Name On").setAutoWidth(true);

        grid.addColumn("results").setAutoWidth(true);
        LitRenderer<Application> resultsOnRenderer = LitRenderer.<Application>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", resultsOn -> resultsOn.isResultsOn() ? "check" : "minus").withProperty("color",
                        resultsOn -> resultsOn.isResultsOn()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(resultsOnRenderer).setHeader("Results On").setAutoWidth(true);

        grid.setItems(query -> applicationService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(APPLICATION_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(AppsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Application.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.application == null) {
                    this.application = new Application();
                }
                binder.writeBean(this.application);
                applicationService.update(this.application);
                clearForm();
                refreshGrid();
                Notification.show("Application details stored.");
                UI.getCurrent().navigate(AppsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the application details.");
            }
        });

    }

    UI ui;
    Dialog notif;

    @Override
    public void onAttach(AttachEvent e) {
        ui = UI.getCurrent();
        notif = new Dialog(new Paragraph("Waiting for a successful fly.io login."));
        notif.setVisible(false);
        notif.setModal(true);
        new Thread(() -> {
            String[] token = new String[1];
            while (token[0] == null) {
                VaadinSession session = e.getUI().getSession();
                session.access(() -> {
                    token[0] = (String) session.getAttribute("ACCESS_TOKEN");
                    logger.warn("inside session {} access token {}", session, token[0]);
                });
                logger.warn("outside session access token {}", token[0]);
                if (token[0] == null) {
                    openNotification(token[0]);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        System.err.println("interrupted");
                    }
                } else {
                    System.err.println("closing");
                    closeNotification(token[0]);
                    System.err.println("closed");
                }

            }
        }).start();
    }

    private void openNotification(String token) {
        ui.access(() -> {
            logger.warn("opening {} since token {}", notif, token);
            notif.setVisible(true);
        });
    }

    private void closeNotification(String token) {
        ui.access(() -> {
            logger.warn("closing {} since token {}", notif, token);
            notif.setVisible(false);
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> applicationId = event.getRouteParameters().get(APPLICATION_ID).map(UUID::fromString);
        if (applicationId.isPresent()) {
            Optional<Application> applicationFromBackend = applicationService.get(applicationId.get());
            if (applicationFromBackend.isPresent()) {
                populateForm(applicationFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested application was not found, ID = %s", applicationId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(AppsView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        name = new TextField("Name");
        nameOn = new Checkbox("Name On");
        results = new TextField("Results");
        resultsOn = new Checkbox("Results On");
        Component[] fields = new Component[] { name, nameOn, results, resultsOn };

        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
        splitLayout.setSplitterPosition(40D);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Application value) {
        this.application = value;
        binder.readBean(this.application);

    }
}
