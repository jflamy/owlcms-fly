package ca.lerta.fly.data.entity;

import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;

@Entity
public class Bundle extends AbstractEntity {
    @NotEmpty(message = "Name cannot be empty")
    private String owlcmsName;

    @NotEmpty(message = "Name cannot be empty")
    private String resultsName;

    private String bundleName;

    private Boolean owlcmsActualRunning;
    private Boolean resultsActualRunning;
    private Boolean dBActualRunning;
    private String dBName;

    private Boolean owlcmsDesiredRunning;

    public String getBundleName() {
        return bundleName;
    }

    public boolean isOwlcmsActualRunning() {
        return owlcmsActualRunning;
    }

    public boolean isResultsActualRunning() {
        return resultsActualRunning;
    }

    public boolean isdBActualRunning() {
        return dBActualRunning;
    }

    public void setdBActualRunning(boolean dBActualRunning) {
        this.dBActualRunning = dBActualRunning;
    }

    public String getdBName() {
        return dBName;
    }

    public void setdBName(String dBName) {
        this.dBName = dBName;
    }

    public String getOwlcmsName() {
        return owlcmsName;
    }

    public void setOwlcmsName(String owlcmsName) {
        this.owlcmsName = owlcmsName;
    }

    public String getResultsName() {
        return resultsName;
    }

    public void setResultsName(String resultsName) {
        this.resultsName = resultsName;
    }

    public String getDatabaseName() {
        return getOwlcmsName() + "-db";
    }

    @Override
    public String toString() {
        return "AppBundle [owlcmsName=" + owlcmsName + ", resultsName=" + resultsName + "]";
    }

    public void setBundleName(String key) {
        this.bundleName = key;
    }

    public void setOwlcmsActualRunning(boolean running) {
        this.owlcmsActualRunning = running;
    }

    public boolean isDBActualRunning() {
        return dBActualRunning;
    }

    public void setDBActualRunning(boolean dbActualRunning) {
        this.dBActualRunning = dbActualRunning;
    }

    public void setResultsActualRunning(boolean running) {
        this.resultsActualRunning = running;
    }

    public void setDBName(String name) {
        this.dBName = name;
    }

    public void setOwlcmsDesiredRunning(Boolean value) {
        this.owlcmsDesiredRunning = value;
    }

    public boolean isOwlcmsDesiredRunning() {
        return this.owlcmsDesiredRunning != null ? this.owlcmsDesiredRunning : isOwlcmsActualRunning();
    }


}
