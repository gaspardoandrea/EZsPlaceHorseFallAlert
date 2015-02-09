package org.ezsplace.horsefallalert;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationListener implements SensorEventListener {
    final float[] mValuesMagnet      = new float[3];
    final float[] mValuesAccelerator = new float[3];
    final float[] mValuesOrientation = new float[3];
    final float[] mRotationMatrix    = new float[9];
    private int value;

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Handle the events for which we registered
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mValuesAccelerator, 0, 3);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
                break;
        }
        SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccelerator, mValuesMagnet);
        SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);
        int val = (int) Math.abs(mValuesAccelerator[0] * 10);
        setValue(val);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
