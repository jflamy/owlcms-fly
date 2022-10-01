package ca.lerta.fly.views.bundles;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import ca.lerta.fly.data.entity.Bundle;

public class BundleEditingForm extends FormLayout {
    private TextField owlcmsName = new TextField("Owlcms Application Name");
    private TextField resultsName = new TextField("Public Scoreboard Application Name");
    private BeanValidationBinder<Bundle> binder = new BeanValidationBinder<>(Bundle.class);

    BundleEditingForm() {
        owlcmsName.setHelperText("Give the name for your main owlcms application.  '.fly.dev' will be added.");
        resultsName.setHelperText("Give the name for the public scoreboard application.  '.fly.dev' will be added.");
        binder.bindInstanceFields(this);

        owlcmsName.addBlurListener(e -> {
            if (resultsName.getValue() == null || resultsName.getValue().isBlank()) {
                resultsName.setValue(owlcmsName.getValue()+"-scoreboard");
            }
        });

        this.add(owlcmsName, resultsName);
    }

    public BundleEditingForm(Bundle ab) {
        this();
        binder.readBean(ab);
    }

    public Binder<Bundle> getBinder() {
        return binder;
    }
}
