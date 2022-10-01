package ca.lerta.fly.data.entity;

import javax.validation.constraints.NotEmpty;

public class AppBundle {
    @NotEmpty(message = "Name cannot be empty")
    private String owlcmsName;
    
    @NotEmpty(message = "Name cannot be empty")
    private String resultsName;

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
        return getOwlcmsName()+"-db";
    }
    @Override
    public String toString() {
        return "AppBundle [owlcmsName=" + owlcmsName + ", resultsName=" + resultsName + "]";
    }
}
