package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

/**
 * Created by marti on 08.07.2017.
 */

public class APIError {

    private String field;
    private String validation;
    private String message;

    @Override
    public String toString() {
        return "APIError{" +
                "field='" + field + '\'' +
                ", validation='" + validation + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public APIError() {
    }

    public static APIError fromJson(String json) {
        return SzUtils.gson.fromJson(json, APIError.class);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}