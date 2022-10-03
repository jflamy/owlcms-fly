package ca.lerta.fly.data.entity;

import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;

import ca.lerta.fly.utils.CommandUtils;

@Entity
public class Bundle extends AbstractEntity {

    private String bundleName;

    @NotEmpty(message = "Name cannot be empty")
    private String owlcmsName;
    private Boolean owlcmsActualRunning;
    private Boolean owlcmsDesiredRunning;

    @NotEmpty(message = "Name cannot be empty")
    private String resultsName;
    private Boolean resultsActualRunning;
    private Boolean resultsDesiredRunning;

    private String dBName;
    private Boolean dBActualRunning;
    private Boolean dBDesiredRunning;

    public Boolean getDBDesiredRunning() {
        return dBDesiredRunning;
    }

    public void setDBDesiredRunning(Boolean dBDesiredRunning) {
        this.dBDesiredRunning = dBDesiredRunning;
    }

    public Boolean getResultsDesiredRunning() {
        return resultsDesiredRunning;
    }

    public void setResultsDesiredRunning(Boolean resultsDesiredRunning) {
        this.resultsDesiredRunning = resultsDesiredRunning;
    }

    public Boolean getOwlcmsActualRunning() {
        return owlcmsActualRunning;
    }

    public void setOwlcmsActualRunning(Boolean owlcmsActualRunning) {
        this.owlcmsActualRunning = owlcmsActualRunning;
    }

    public Boolean getResultsActualRunning() {
        return resultsActualRunning;
    }

    public void setResultsActualRunning(Boolean resultsActualRunning) {
        this.resultsActualRunning = resultsActualRunning;
    }

    public Boolean getdBActualRunning() {
        return dBActualRunning;
    }

    public void setdBActualRunning(Boolean dBActualRunning) {
        this.dBActualRunning = dBActualRunning;
    }

    public Boolean getOwlcmsDesiredRunning() {
        return owlcmsDesiredRunning;
    }

    public String getBundleName() {
        return bundleName;
    }

    public Boolean isOwlcmsActualRunning() {
        return owlcmsActualRunning;
    }

    public Boolean isResultsActualRunning() {
        return resultsActualRunning;
    }

    public Boolean isdBActualRunning() {
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

    public void syncWithRemote(String accessToken) {
        if (owlcmsActualRunning != null && owlcmsDesiredRunning != owlcmsActualRunning) {
            if (Boolean.TRUE.equals(owlcmsDesiredRunning)) {
                // start database if not running, then application
                if (dBName != null) {
                    var processBuilder = new ProcessBuilder();
                    processBuilder.command(CommandUtils.getCommandArgs("appScale", accessToken, dBName, 1));
                    CommandUtils.getProcessOutput(processBuilder);
                }
                if (owlcmsName != null) {
                    var processBuilder = new ProcessBuilder();
                    processBuilder.command(CommandUtils.getCommandArgs("appScale", accessToken, owlcmsName, 1));
                    CommandUtils.getProcessOutput(processBuilder);
                }
            } else {
                // stop owlcms, then stop database.
                if (owlcmsName != null) {
                    var processBuilder = new ProcessBuilder();
                    processBuilder.command(CommandUtils.getCommandArgs("appScale", accessToken, owlcmsName, 0));
                    CommandUtils.getProcessOutput(processBuilder);
                }
                if (dBName != null) {
                    var processBuilder = new ProcessBuilder();
                    processBuilder.command(CommandUtils.getCommandArgs("appScale", accessToken, dBName, 0));
                    CommandUtils.getProcessOutput(processBuilder);
                }
            }
        }
        if (resultsActualRunning != null && resultsDesiredRunning != resultsActualRunning) {
            if (resultsName != null) {
                var processBuilder = new ProcessBuilder();
                processBuilder.command(CommandUtils.getCommandArgs("appScale", accessToken, resultsName,
                        Boolean.TRUE.equals(resultsDesiredRunning) ? 1 : 0));
                CommandUtils.getProcessOutput(processBuilder);
            }
        }
    }
}
