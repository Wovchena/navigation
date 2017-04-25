package com.mycompany.myfirstindoorsapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Work with accel such rotate vectors of a.
 */

public class Accel implements SensorEventListener {
    private SensorManager mSensorManager;
    private float[] accelData = new float[3];
    private float[] magnetData = new float[3];
    private float[] rotationMatrix = new float[16];
    private float[] OrientationData = new float[3];

    public Accel(SensorManager sm) // you can get SensorManager by msensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    {
        // TODO also pass map rotation from indoo.rs
        // TODO also pass instance of Kalman
        mSensorManager = sm;
    }

    public void start(int mode) {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
        // TYPE_ACCELEROMETER for calibration
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final int type = sensorEvent.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            accelData = sensorEvent.values.clone();
        }

        if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetData = sensorEvent.values.clone();
        }
        SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData);
        SensorManager.getOrientation(rotationMatrix, OrientationData);
        //TODO rotate and call method of Kalman to work with this data
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        switch (i) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                Log.d("AccelLog", "ENSOR_STATUS_ACCURACY_HIGH");
                break;
            default:
                Log.d("AccelLog", "ENSOR_STATUS_ACCURACY_ not high");
        }


    }
}