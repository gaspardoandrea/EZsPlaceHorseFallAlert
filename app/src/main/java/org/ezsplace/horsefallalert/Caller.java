package org.ezsplace.horsefallalert;

import org.ezsplace.horsefallalert.intent.AlertInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;


public class Caller implements Runnable, CallerEventListener {
    private final Queue<AlertInterface> phoneStack;
    private final int delayBetweenCalls;
    private volatile boolean running;
    private Thread thread;
    private List<CallerEventListener> listeners = new ArrayList<CallerEventListener>();

    public Caller(Queue<AlertInterface> phone, int mDelayBetweenCalls) {
        phoneStack = phone;
        delayBetweenCalls = mDelayBetweenCalls;
        start();
    }

    public void start() {
        if (!running) {
            running = true;
            thread = new Thread(this, "Caller");
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stop() {
        if (running) {
            running = false;
            thread = null;
        }
    }

    @Override
    public void run() {
        fireCallingStarted();
        while (!phoneStack.isEmpty()) {
            doCall(phoneStack.remove());
            if (!running) {
                phoneStack.clear();
                fireCallingEnded();
                return;
            }
            try {
                Thread.sleep(delayBetweenCalls);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        running = false;
        fireCallingEnded();
    }

    private void fireCallingStarted() {
        for (CallerEventListener listener : listeners) {
            listener.callingStarted();
        }
    }

    private void fireCallingEnded() {
        for (CallerEventListener listener : listeners) {
            listener.callingEnded();
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void callingStarted() {
    }

    @Override
    public void callingEnded() {
        running = false;
    }

    @Override
    public void alert(AlertInterface phoneNumber) {
    }

    private void doCall(AlertInterface phone) {
        for (CallerEventListener listener : listeners) {
            listener.alert(phone);
        }
    }

    public void addCallerEventListener(CallerEventListener listener) {
        listeners.add(listener);
    }

    public void removeCallerEventListener(CallerEventListener listener) {
        listeners.remove(listener);
    }
}

