package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

/**
 * Created by marti on 08.07.2017.
 */

public class ApiMessage {

    private String field;
    private String validation;
    private String message;

    @Override
    public String toString() {
        return "ApiMessage{" +
                "field='" + field + '\'' +
                ", validation='" + validation + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public ApiMessage() {
    }

    public static ApiMessage fromJson(String json) {
        return SzUtils.gson.fromJson(json, ApiMessage.class);
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
    @SuppressWarnings("SameParameterValue")
    public void setMessage(String message) {
        this.message = message;
    }
}