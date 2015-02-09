package org.ezsplace.horsefallalert.intent;

public class PhoneCallIntent extends AbstractAlert {
    public PhoneCallIntent(int mProgressiveNumber, String mPhoneNumber) {
        super(mProgressiveNumber, mPhoneNumber, AlertType.PhoneCall);
    }
}
