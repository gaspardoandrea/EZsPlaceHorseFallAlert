package org.ezsplace.horsefallalert.intent;

/**
 * Created by andreag on 29/12/14.
 */
public class AbstractAlert implements AlertInterface {
    private final int progressiveNumber;
    private final String phoneNumber;
    private final AlertType alertType;

    public AbstractAlert(final int mProgressiveNumber,
                         final String mPhoneNumber,
                         final AlertType pAlertType) {
        alertType = pAlertType;
        phoneNumber = mPhoneNumber;
        progressiveNumber = mProgressiveNumber;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public AlertType getAlertType() {
        return alertType;
    }

    @Override
    public int progressiveNumber() {
        return progressiveNumber;
    }
}
