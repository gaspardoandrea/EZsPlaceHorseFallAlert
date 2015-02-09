package org.ezsplace.horsefallalert.intent;

public interface AlertInterface {
    String getPhoneNumber();
    AlertType getAlertType();
    int progressiveNumber();
}
