package org.ezsplace.horsefallalert;

import org.ezsplace.horsefallalert.intent.AlertInterface;

import java.util.EventListener;

public interface CallerEventListener extends EventListener {
    void callingStarted();
    void callingEnded();
    void alert(AlertInterface phoneNumber);
}
