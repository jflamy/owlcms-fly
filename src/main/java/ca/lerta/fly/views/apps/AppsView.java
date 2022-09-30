package ca.lerta.fly.views.apps;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.PermitAll;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import ca.lerta.fly.data.entity.FlyApplication;
import ca.lerta.fly.data.service.FlyApplicationRepository;
import ca.lerta.fly.data.service.FlyApplicationService;
import ca.lerta.fly.security.TokenAuthentication;
import ca.lerta.fly.views.MainLayout;
import ch.qos.logback.classic.Logger;

@PageTitle("Fly.io owlcms Applications")
@Route(value = "apps/:applicationID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
@AnonymousAllowed
@Uses(Icon.class)

/**
 * Show users own applications.
 * 
 * Quirky implementation - the table is stored in a repository stored in the
 * session. Because we update from background threads, much of the Spring
 * service magic does not work.
 * 
 * So we use the battle-tested direct to repo approach. And we don't need paging
 * either.
 */
public class AppsView extends Div implements TokenAuthentication {
    Logger logger = (Logger) LoggerFactory.getLogger(AppsView.class);

    private final String APPLICATION_ID = "applicationID";
    private final String APPLICATION_EDIT_ROUTE_TEMPLATE = "apps/%s/edit";

    private Grid<FlyApplication> grid = new Grid<>(FlyApplication.class, false);

    private TextField name;
    private Checkbox running;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<FlyApplication> binder;

    private FlyApplication flyApplication;
    private final FlyApplicationService flyApplicationService;

    private FlyApplicationRepository flyApplicationRepository;
    private String accessToken;

    private SplitLayout splitLayout;

    @Autowired
    public AppsView(FlyApplicationService flyApplicationService) {
        this.flyApplicationService = flyApplicationService;
        addClassNames("apps-view");

        // Create UI
        splitLayout = new SplitLayout();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);

        // Configure Grid
        configureGrid(flyApplicationService);

        // Configure Form
        binder = new BeanValidationBinder<>(FlyApplication.class);
        binder.bindInstanceFields(this);

        // Buttons
        configureButtons(flyApplicationService);
    }

    @Override
    public void onAttach(AttachEvent e) {
        UI.getCurrent();
        flyApplicationRepository = flyApplicationService.getRepository();
        VaadinSession session = e.getUI().getSession();
        session.accessSynchronously(() -> {
            accessToken = (String) session.getAttribute("ACCESS_TOKEN");
            logger.debug("session {} access token {}", session, accessToken);
        });
        if (accessToken == null) {
            throw new RuntimeException("token not present, can't happen");
        } else {
            flyApplicationRepository.loadRepository(accessToken);
            populateGrid(flyApplicationRepository);
            refreshGrid();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        checkToken(event);
        Optional<UUID> applicationId = event.getRouteParameters().get(APPLICATION_ID).map(UUID::fromString);
        if (applicationId.isPresent()) {
            Optional<FlyApplication> applicationFromBackend = flyApplicationService.get(applicationId.get());
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

    private void configureButtons(FlyApplicationService flyApplicationService) {
        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.flyApplication == null) {
                    this.flyApplication = new FlyApplication();
                }
                binder.writeBean(this.flyApplication);
                flyApplicationService.update(this.flyApplication);
                clearForm();
                refreshGrid();
                Notification.show("Fly Application details stored.");
                UI.getCurrent().navigate(AppsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the application details.");
            }
        });
    }

    private void configureGrid(FlyApplicationService flyApplicationService) {
        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        LitRenderer<FlyApplication> runningRenderer = LitRenderer.<FlyApplication>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", running -> running.isRunning() ? "check" : "minus").withProperty("color",
                        running -> running.isRunning()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(runningRenderer).setHeader("Status").setAutoWidth(true);
        grid.addColumn("bundle").setAutoWidth(true);

        populateGrid(flyApplicationService.getRepository());
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
        clearForm();
    }

    private void populateGrid(FlyApplicationRepository flyApplicationRepository) {
        // grid.setItems(query -> flyApplicationRepository.list(
        // PageRequest.of(query.getPage(), query.getPageSize(),
        // VaadinSpringDataHelpers.toSpringDataSort(query)))
        // .stream());
        grid.setItems(flyApplicationRepository.findAll());
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        name = new TextField("Name");
        running = new Checkbox("Running");
        Component[] fields = new Component[] { name, running };

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
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setMargin(false);
        Button newBundle = new Button("Create new application bundle");
        buttonLayout.add(newBundle);
        wrapper.add(buttonLayout, grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getListDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(FlyApplication value) {
        splitLayout.getSecondaryComponent().setVisible(value != null);
        this.flyApplication = value;
        if (binder != null) {
            binder.readBean(this.flyApplication);
        }

    }
}
