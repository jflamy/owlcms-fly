package ca.lerta.fly.views.bundles;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.security.PermitAll;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import ca.lerta.fly.data.entity.Bundle;
import ca.lerta.fly.data.service.BundleRepository;
import ca.lerta.fly.data.service.BundleService;
import ca.lerta.fly.security.TokenAuthentication;
import ca.lerta.fly.utils.CommandUtils;
import ca.lerta.fly.views.MainLayout;
import ch.qos.logback.classic.Logger;

@PageTitle("Fly.io owlcms Application Bundles")
@Route(value = "bundles/:bundleID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
@AnonymousAllowed
@Uses(Icon.class)

/**
 * Show users own application bundles.
 * 
 * Quirky implementation - the table is stored in a repository stored in the
 * session. Because we update from background threads, much of the Spring
 * service magic does not work.
 * 
 * So we use the battle-tested direct to repo approach. And we don't need paging
 * either.
 */
public class BundlesView extends Div implements TokenAuthentication {
    Logger logger = (Logger) LoggerFactory.getLogger(BundlesView.class);

    private final String BUNDLE_ID = "bundleID";
    private final String BUNDLE_EDIT_ROUTE_TEMPLATE = "bundles/%s/edit";

    private Grid<Bundle> grid = new Grid<>(Bundle.class, false);

    private TextField name;
    private Checkbox running;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Bundle> binder;

    private Bundle Bundle;
    private final BundleService bundleService;

    private BundleRepository BundleRepository;
    private String accessToken;

    private SplitLayout splitLayout;

    @Autowired
    public BundlesView(BundleService bundleService) {
        this.bundleService = bundleService;
        addClassNames("apps-view");

        // Create UI
        splitLayout = new SplitLayout();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);

        // Configure Grid
        configureGrid(bundleService);

        // // Configure Form
        // binder = new BeanValidationBinder<>(Bundle.class);
        // binder.bindInstanceFields(this);

        // Buttons
        configureButtons(bundleService);
    }

    @Override
    public void onAttach(AttachEvent e) {
        UI.getCurrent();
        BundleRepository = bundleService.getRepository();
        VaadinSession session = e.getUI().getSession();
        session.accessSynchronously(() -> {
            accessToken = (String) session.getAttribute("ACCESS_TOKEN");
            logger.debug("session {} access token {}", session, accessToken);
        });
        if (accessToken == null) {
            throw new RuntimeException("token not present, can't happen");
        } else {
            BundleRepository.loadRepository(accessToken);
            populateGrid(BundleRepository);
            refreshGrid();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        checkToken(event);
        Optional<UUID> bundleId = event.getRouteParameters().get(BUNDLE_ID).map(UUID::fromString);
        if (bundleId.isPresent()) {
            Optional<Bundle> bundleFromBackend = bundleService.get(bundleId.get());
            if (bundleFromBackend.isPresent()) {
                populateForm(bundleFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested bundle was not found, ID = %s", bundleId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(BundlesView.class);
            }
        }
    }

    private void configureButtons(BundleService BundleService) {
        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.Bundle == null) {
                    this.Bundle = new Bundle();
                }
                binder.writeBean(this.Bundle);
                BundleService.update(this.Bundle);
                clearForm();
                refreshGrid();
                Notification.show("Fly application bundle details stored.");
                UI.getCurrent().navigate(BundlesView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the application bundle details.");
            }
        });
    }

    private void configureGrid(BundleService BundleService) {
        // Configure Grid
        grid.addColumn("bundleName").setAutoWidth(true);

        SerializableBiConsumer<ToggleButton, Bundle> owlcmsRunningConsumer = (toggle, bundle) -> {
            toggle.setValue(bundle.isOwlcmsActualRunning());
            toggle.addValueChangeListener(e -> {
                bundle.setOwlcmsDesiredRunning(e.getValue());
                var accessToken = (String) VaadinSession.getCurrent().getAttribute("ACCESS_TOKEN");
                var accessToken2 = CommandUtils.getAccessToken();
                logger.warn("1============={} {}",accessToken,accessToken2);
                bundle.syncWithRemote(accessToken);
                refreshGrid();
            });
        };
        ComponentRenderer<ToggleButton, Bundle> owlcmsRunningRenderer = new ComponentRenderer<>(ToggleButton::new,
                owlcmsRunningConsumer);
        grid.addColumn(owlcmsRunningRenderer).setHeader("owlcms on/off").setAutoWidth(true);

        SerializableBiConsumer<ToggleButton, Bundle> resultsRunningConsumer = (toggle, bundle) -> {
            if (bundle.isResultsActualRunning() != null) {
                toggle.setValue(bundle.isResultsActualRunning());
                toggle.addValueChangeListener(e -> {
                    bundle.setResultsDesiredRunning(e.getValue());
                    var accessToken = (String) VaadinSession.getCurrent().getAttribute("ACCESS_TOKEN");
                    var accessToken2 = CommandUtils.getAccessToken();
                    logger.warn("2============={} {}",accessToken,accessToken2);
                    bundle.syncWithRemote(accessToken);;
                    refreshGrid();
                });
            } else {
                toggle.setVisible(false);
            }
        };
        ComponentRenderer<ToggleButton, Bundle> resultsRunningRenderer = new ComponentRenderer<>(ToggleButton::new,
                resultsRunningConsumer);
        grid.addColumn(resultsRunningRenderer).setHeader("public scoreboard on/off").setAutoWidth(true);

        populateGrid(BundleService.getRepository());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(BUNDLE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(BundlesView.class);
            }
        });
        clearForm();
    }

    private void populateGrid(BundleRepository BundleRepository) {
        // grid.setItems(query -> BundleRepository.list(
        // PageRequest.of(query.getPage(), query.getPageSize(),
        // VaadinSpringDataHelpers.toSpringDataSort(query)))
        // .stream());
        grid.setItems(BundleRepository.findAll());
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new ResponsiveStep("0", 1, LabelsPosition.TOP));
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
        newBundle.addClickListener((e) -> bundleCreationDialog(appBundle -> System.err.println(appBundle.toString())));
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

    private void populateForm(Bundle value) {
        splitLayout.getSecondaryComponent().setVisible(value != null);
        this.Bundle = value;
        if (binder != null) {
            binder.readBean(this.Bundle);
        }
    }

    private Dialog bundleCreationDialog(Consumer<Bundle> creationCallback) {
        Dialog d = new Dialog();
        Bundle newBundle = new Bundle();
        BundleEditingForm bundleEdit = new BundleEditingForm(newBundle);
        d.add(bundleEdit);
        Button saveButton = new Button("Save", (e) -> {
            try {
                bundleEdit.getBinder().writeBean(newBundle);
                creationCallback.accept(newBundle);
            } catch (ValidationException e1) {
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", (e) -> {
            d.close();
        });
        d.getFooter().add(saveButton, cancelButton);
        d.open();
        return d;
    }
}
