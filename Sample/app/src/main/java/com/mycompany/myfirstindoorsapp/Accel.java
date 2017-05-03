package com.mycompany.myfirstindoorsapp;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

/**
 * Work with accel such rotate vectors of a.
 */

public class Accel implements SensorEventListener {
    PagedActivity pa;
    private SensorManager mSensorManager;
    private float[] accelData = new float[3];
    private float[] magnetData = new float[3];
    private float[] gravityData = new float[3];
    private float[] rotationMatrix = new float[16];
    private float[] OrientationData = new float[3];
    private double[] aInBasic=new double[3];
    private double[] doubleRotationMatrix=new double[9];
    private double[] doubleAccelData=new double[3];
    private double rotation;
    private double[] aInReal=new double[2];

    public Accel(PagedActivity pa, SensorManager sm, float rotationInDeg) // you can get
    // SensorManager
    // by
    // msensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    {
        //TODO it is possible to pass info to Kalman wothout pa
        mSensorManager = sm;
        this.pa=pa;
        magnetData[0]=magnetData[1]=magnetData[2]=0;
        gravityData[0]=gravityData[1]=gravityData[2]=0;
        this.rotation= Math.toRadians(rotationInDeg);

    }

    public void start(int mode) {

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
        // TYPE_ACCELEROMETER for calibration
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final int type = sensorEvent.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            accelData = sensorEvent.values.clone();
            SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData);
            SensorManager.getOrientation(rotationMatrix, OrientationData);
            for (int i=0; i<9; i++)
            {
                doubleRotationMatrix[i]=rotationMatrix[i];
            }
            for (int i=0; i<3; i++)
            {
                doubleAccelData[i]=accelData[i];
            }
            aInBasic=(new Array2DRowRealMatrix(doubleRotationMatrix)).operate(doubleAccelData);

            aInReal[0]=aInBasic[0]*Math.cos(rotation)-aInBasic[1]*Math.sin(rotation);
            aInReal[1]=aInBasic[0]*Math.sin(rotation)+aInBasic[1]*Math.cos(rotation);
            //TODO is rotation right?
            //pass only x and y in accelData to onAccelCahnged()
            pa.onAccelChanged(aInReal);
        }

        if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetData = sensorEvent.values.clone();
        }
        if (type == Sensor.TYPE_GRAVITY) {
            gravityData = sensorEvent.values.clone();
        }

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