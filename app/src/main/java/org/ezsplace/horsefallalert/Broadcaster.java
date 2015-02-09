package org.ezsplace.horsefallalert;

import android.content.Context;
import android.content.Intent;

public class Broadcaster {
    public static final String ACTION_FROM_SERVICE = "org.ezsplace.horsefallalert.fromtheservice";
    public static final String SETTINGS_STOPPED = "org.ezsplace.horsefallalert.settingsstopped";
    public static final String DEBUG_ANGLE = "org.ezsplace.horsefallalert.debugangle";

    public static final String ACTION_FROM_SERVICE_ANGLE = "org.ezsplace.horsefallalert.fromtheservice_angle";
    public static final String ACTION_FROM_SERVICE_STATUS = "org.ezsplace.horsefallalert.fromtheservice_status";
    public static final String ACTION_FROM_SERVICE_ALERT = "org.ezsplace.horsefallalert.fromtheservice_alert";
    public static final String ACTION_FROM_SERVICE_WAKE = "org.ezsplace.horsefallalert.fromtheservice_wake";
    public static final String DEBUG_ANGLE_VALUE = "org.ezsplace.horsefallalert.debuganglevalue";

    private static String angleValue;
    private static String statusValue;
    private static String alertValue;

    public static void updateAngle(Context context, String angle) {
        angleValue = angle;
        notifyAll(context);
    }

    public static void updateStatus(Context context, String status) {
        statusValue = status;
        notifyAll(context);
    }

    public static void updateAlert(Context context, String alert) {
        alertValue = alert;
        notifyAll(context);
    }

    private static void notifyAll(Context context) {
        notifyAll(context, angleValue, statusValue, alertValue);
    }

    private static void notifyAll(Context context, String angle, String status, String alert) {
        notifyAll(context, angle, status, alert, false);
    }

    private static void notifyAll(Context context, String angle, String status, String alert, boolean wake) {
        final Intent broadcastIntent = new Intent(ACTION_FROM_SERVICE);
        broadcastIntent.putExtra(ACTION_FROM_SERVICE_ANGLE, angle);
        broadcastIntent.putExtra(ACTION_FROM_SERVICE_STATUS, status);
        broadcastIntent.putExtra(ACTION_FROM_SERVICE_ALERT, alert);
        broadcastIntent.putExtra(ACTION_FROM_SERVICE_WAKE, wake);
        context.sendBroadcast(broadcastIntent);
    }

    public static void updateAngle(Context context) {
        updateAngle(context, "");
    }

    public static void notifyAlert(Context context) {
        notifyAll(context, angleValue, statusValue, alertValue, true);
    }

    public static void settingStopped(Context context) {
        final Intent broadcastIntent = new Intent(SETTINGS_STOPPED);
        context.sendBroadcast(broadcastIntent);
    }

    public static void updateDebugAngle(Context applicationContext, int value) {
        final Intent broadcastIntent = new Intent(DEBUG_ANGLE);
        broadcastIntent.putExtra(DEBUG_ANGLE_VALUE, value);
        applicationContext.sendBroadcast(broadcastIntent);
    }
}
