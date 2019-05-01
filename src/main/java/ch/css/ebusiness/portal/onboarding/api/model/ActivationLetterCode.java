package ch.css.ebusiness.portal.onboarding.api.model;

public class ActivationLetterCode {
    private String activationCode;

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    @Override
    public String toString() {
        return "ActivationLetterCode{" +
                "activationCode='" + activationCode + '\'' +
                '}';
    }
}
