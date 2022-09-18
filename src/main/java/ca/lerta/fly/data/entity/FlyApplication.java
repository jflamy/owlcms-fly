package ca.lerta.fly.data.entity;

import javax.persistence.Entity;

@Entity
public class FlyApplication extends AbstractEntity {

    private String name;
    private boolean running;
    private String bundle;
    private String label;
    private boolean redeployRequested;
    private boolean deployed;

    public boolean isDeployed() {
        return deployed;
    }

    public void setDeployed(boolean deployed) {
        this.deployed = deployed;
    }

    public boolean isRedeployRequested() {
        return redeployRequested;
    }

    public void setRedeployRequested(boolean redeployRequested) {
        this.redeployRequested = redeployRequested;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
