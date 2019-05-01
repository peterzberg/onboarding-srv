package ch.css.ebusiness.portal.onboarding.api.model;

public class DocumentCode {
    private String ahvNumber;
    private String documentNumber;

    public String getAhvNumber() {
        return ahvNumber;
    }

    public void setAhvNumber(String ahvNumber) {
        this.ahvNumber = ahvNumber;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    @Override
    public String toString() {
        return "DocumentCode{" +
                "ahvNumber='" + ahvNumber + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                '}';
    }
}
