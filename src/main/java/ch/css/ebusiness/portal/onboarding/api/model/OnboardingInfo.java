package ch.css.ebusiness.portal.onboarding.api.model;

import java.util.Map;

public class OnboardingInfo {
    private String id;
    private Map<String, Object> variables;
    private String currentStep;
    private boolean ended = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    @Override
    public String toString() {
        return "OnboardingInfo{" +
                "id='" + id + '\'' +
                ", variables=" + variables +
                ", currentStep='" + currentStep + '\'' +
                ", ended=" + ended +
                '}';
    }

    public void setCurrentStep(final String currentStep) {
        this.currentStep = currentStep;
    }

    public final String getCurrentStep() {
        return currentStep;
    }
}
