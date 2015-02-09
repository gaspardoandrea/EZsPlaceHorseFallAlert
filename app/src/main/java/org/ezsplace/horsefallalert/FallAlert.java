package org.ezsplace.horsefallalert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

import org.ezsplace.horsefallalert.intent.AlertInterface;
import org.ezsplace.horsefallalert.intent.AlertType;
import org.ezsplace.horsefallalert.intent.PhoneCallIntent;
import org.ezsplace.horsefallalert.intent.SmsIntent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class FallAlert implements Runnable, CallerEventListener {
    private static final long INTERVAL = 250;
    private static final long LONG_INTERVAL = 600000;
    private volatile boolean running;
    private Thread thread;
    private AtomicBoolean isDown;
    private AtomicLong lastStatusChange = new AtomicLong(0);
    private long currentValue;
    private SharedPreferences sharedPrefs;
    private Caller caller;
    private static RotationListener rotationListener = new RotationListener();
    private SensorManager mSensorManager;
    private Context context;
    private boolean sensorRegistered;
    private PowerManager.WakeLock lock;

    public FallAlert(Context startingContext) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(startingContext);
        mSensorManager = (SensorManager) startingContext.getSystemService(Context.SENSOR_SERVICE);
        isDown = new AtomicBoolean(false);
        context = startingContext;
        registerSensors();
        start();

    }

    private void start() {
        if (!running) {
            running = true;
            thread = new Thread(this, "Fall alert thread");
            thread.setDaemon(false);
            thread.start();
        }
    }

    public void stop() {
        Log.d("FallAlertService", "stop");
        if (running) {
            running = false;
            thread = null;
        }
    }

    private boolean isDown() {
        return isDown.get();
    }

    public void run() {
        boolean wasDown = false;
        AlertLogManager.register(context, "Thread started");
        while (running) {
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (caller != null && caller.isRunning()) {
                continue;
            }
            setValue(rotationListener.getValue());
            Broadcaster.updateAngle(getContext(), String.valueOf(currentValue));
            if (isDown()) {
                String msg = String.format(getString("is_down"), getHorseName());
                Broadcaster.updateStatus(getContext(), msg);
                Broadcaster.updateAlert(getContext(), String.format(getString("calling_in"), getSecsToNextCall()));
                if (!wasDown) {
                    Broadcaster.notifyAlert(getContext());
                    AlertLogManager.register(context, msg);
                }
            } else {
                String msg = String.format(getString("is_up"), getHorseName());
                Broadcaster.updateStatus(getContext(), msg);
                Broadcaster.updateAlert(getContext(), getString("ok"));
                if (caller != null && caller.isRunning()) {
                    caller.stop();
                }
                if (wasDown) {
                    AlertLogManager.register(context, msg);
                }
            }
            wasDown = isDown();
            if (lastStatusChange.get() != 0 && isDown() && downFromMoreThan()) {
                consumeQueueCall(getPhoneQueue());
                try {
                    Thread.sleep(LONG_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        AlertLogManager.register(context, "Thread stopped");
    }

    private void setValue(int value) {
        boolean down = value > getAlpha();
        currentValue = value;
        if (down != isDown()) {
            isDown.set(down);
            if (!down) {
                if (caller != null && caller.isRunning()) {
                    caller.stop();
                }
            }
            lastStatusChange.set(System.currentTimeMillis());
        }
    }

    private Queue<AlertInterface> getPhoneQueue() {
        String[] phones = sharedPrefs.getString("phone", "NULL").split(",");
        Queue<AlertInterface> rv = new LinkedList<AlertInterface>();
        for (int i = 0; i < getNumberOfCalls(); i++) {
            for (String nr : phones) {
                AlertInterface alert;
                if (isMobileNumber(nr) && mustSendSms() && i == 0) {
                    alert = new SmsIntent(i + 1, nr);
                } else {
                    alert = new PhoneCallIntent(i + 1, nr);
                }
                rv.add(alert);
            }
        }
        return rv;
    }

    private boolean isMobileNumber(String nr) {
        return nr.matches("^[1-9].*");
    }

    private void consumeQueueCall(Queue<AlertInterface> phone) {
        AlertLogManager.register(context, "Start alert queue");
        if (caller != null) {
            caller.removeCallerEventListener(this);
        }
        caller = new Caller(phone, getDelayBetweenCalls());
        caller.addCallerEventListener(this);
    }

    private int getSecsToNextCall() {
        return (int) (getDelay() - (System.currentTimeMillis() - lastStatusChange.longValue()) / 1000);
    }

    private boolean downFromMoreThan() {
        return System.currentTimeMillis() - lastStatusChange.longValue() > 1000 * getDelay();
    }

    private void registerSensors() {
        if (sensorRegistered) {
            return;
        }
        mSensorManager.registerListener(rotationListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(rotationListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorRegistered = true;
    }

    private void unregisterSensors() {
        if (!sensorRegistered) {
            return;
        }
        mSensorManager.unregisterListener(rotationListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(rotationListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        sensorRegistered = false;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public void callingStarted() {
        unregisterSensors();
        PowerManager powerManager = (PowerManager)
                context.getSystemService(Context.POWER_SERVICE);
        lock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "");
        lock.acquire();
    }

    @Override
    public void callingEnded() {
        registerSensors();
        lock.release();
    }

    @Override
    public void alert(AlertInterface alert) {
        if (alert.getAlertType() == AlertType.Sms && mustSendSms()) {
            sendSms(alert);
        } else {
            doCall(alert);
        }
    }

    private void sendSms(AlertInterface phoneNumber) {
        String msg = String.format(getString("sms_to"), phoneNumber.progressiveNumber(), phoneNumber.getPhoneNumber());
        AlertLogManager.register(getContext(), msg);
        Broadcaster.updateAlert(getContext(), getString("sms_now"));
        Broadcaster.updateStatus(getContext(), msg);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber.getPhoneNumber(),
                null,
                String.format(getString("sms"), getHorseName()), null, null);
    }

    private boolean mustSendSms() {
        return sharedPrefs.getBoolean("sms", false);
    }

    private void doCall(AlertInterface phoneNumber) {
        String msg = String.format(getString("calling"), phoneNumber.progressiveNumber(), phoneNumber.getPhoneNumber());
        AlertLogManager.register(getContext(), msg);
        Broadcaster.updateAngle(getContext());
        Broadcaster.updateAlert(getContext(), getString("calling_now"));
        Broadcaster.updateStatus(getContext(), msg);
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber.getPhoneNumber()));
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
    }

    private String getString(final String key) {
        int resId = getContext().getResources().getIdentifier(
                key, "string", getContext().getPackageName());
        return getContext().getResources().getString(resId);
    }

    private String getHorseName() {
        return sharedPrefs.getString("horse_name", "NULL");
    }
    private int getAlpha() {
        return Integer.valueOf(sharedPrefs.getString("alpha", "90"));
    }
    private int getDelay() {
        return Integer.valueOf(sharedPrefs.getString("delay", "15"));
    }
    private int getDelayBetweenCalls() {
        return 1000 * Integer.valueOf(sharedPrefs.getString("delay_between_calls", "30"));
    }

    public int getNumberOfCalls() {
        return Integer.valueOf(sharedPrefs.getString("nr_of_calls", "3"));
    }

    public void updateAngle(int intExtra) {
        rotationListener.setValue(intExtra);
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
