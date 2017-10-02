package eu.unicorn.helper.model;

import eu.unicorn.cgmes.model.CgmesProfileType;
import eu.unicorn.cgmes.model.Model;

import java.util.Optional;

public class ParsingResult {

    private Model model;
    private long parsingTime;
    private Exception exception;
    private CgmesProfileType profileType;

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public long getParsingTime() {
        return parsingTime;
    }

    public void setParsingTime(long parsingTime) {
        this.parsingTime = parsingTime;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public CgmesProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(CgmesProfileType profileType) {
        this.profileType = profileType;
    }

    @Override
    public String toString() {
        return "ParsingResult{" +
                "model=" + model +
                ", parsingTime=" + parsingTime +
                ", exception=" + exception +
                ", profileType=" + profileType +
                '}';
    }
}
