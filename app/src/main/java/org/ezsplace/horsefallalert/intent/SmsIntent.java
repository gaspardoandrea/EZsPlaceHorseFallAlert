package org.ezsplace.horsefallalert.intent;

public class SmsIntent extends AbstractAlert {
    public SmsIntent(int mProgressiveNumber, String mPhoneNumber) {
        super(mProgressiveNumber, mPhoneNumber, AlertType.Sms);
    }
}
