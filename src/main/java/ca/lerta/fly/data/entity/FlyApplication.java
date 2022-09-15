package ca.lerta.fly.data.entity;

import javax.persistence.Entity;

@Entity
public class FlyApplication extends AbstractEntity {

    private String name;
    private boolean nameOn;
    private String results;
    private boolean resultsOn;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isNameOn() {
        return nameOn;
    }
    public void setNameOn(boolean nameOn) {
        this.nameOn = nameOn;
    }
    public String getResults() {
        return results;
    }
    public void setResults(String results) {
        this.results = results;
    }
    public boolean isResultsOn() {
        return resultsOn;
    }
    public void setResultsOn(boolean resultsOn) {
        this.resultsOn = resultsOn;
    }

}
