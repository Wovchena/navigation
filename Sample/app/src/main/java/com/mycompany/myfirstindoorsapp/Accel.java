package com.mycompany.myfirstindoorsapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
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
    private float[] rotationMatrix = new float[9];
    private double[] aInBasic=new double[3];
    private double[][] doubleRotationMatrix=new double[3][3];
    private double[] doubleAccelData=new double[3];
    private double rotation;
    private double[] aInReal=new double[2];
    double [] meanAInReal=new double[2];
    long lastTimeSend = 0;
    int counter = 0;
    double tmp;

    public Accel(PagedActivity pa, SensorManager sm, float rotationInDeg)
    {
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
        long currentT= SystemClock.uptimeMillis();
        final int type = sensorEvent.sensor.getType();

        if (type == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelData = sensorEvent.values.clone();
            if (accelData[0]>0.1) {
                Log.d("accelaccel", "" + accelData[0]);
            }
            SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData);
            int c=0;
            for (int i=0; i<3; i++)
            {
                for (int j=0; j<3; j++) {
                    doubleRotationMatrix[i][j] = rotationMatrix[c];
                    c++;
                }
            }
            for (int i=0; i<3; i++)
            {
                doubleAccelData[i]=accelData[i];
            }
            aInBasic=(new Array2DRowRealMatrix(doubleRotationMatrix)).operate(doubleAccelData);
            if (aInBasic[0]>0.1){
                Log.d("directiondirection", ""+aInBasic[0]+" | "+ aInBasic[1]+" | "+aInBasic[2]);
            }
Log.d("rotatinrotation", ""+rotation);
            tmp=aInBasic[0];
            aInBasic[0]=aInBasic[1];
            aInBasic[1]=tmp;
            aInReal[0]=aInBasic[0]*Math.cos(rotation)+aInBasic[1]*Math.sin(rotation);
            aInReal[0]=aInReal[0]*1000;
            aInReal[1]=aInBasic[0]*Math.sin(rotation)*(-1)+aInBasic[1]*Math.cos(rotation);
            aInReal[1]=aInReal[1]*1000;
            counter++;
            meanAInReal[0]+=aInReal[0];
            meanAInReal[1]+=aInReal[1];
            //pass only x and y in accelData to onAccelCahnged()
            if (currentT-lastTimeSend>=2) {//passed 2 millisec
                meanAInReal[0]=meanAInReal[0]/counter;
                meanAInReal[1]=meanAInReal[1]/counter;
                if ((meanAInReal[0]>2)||(meanAInReal[1]>2)||(meanAInReal[0]<-2)
                        ||meanAInReal[1]<-2) {
                    Log.d("accelaccels", "" + meanAInReal[0] + " | " + meanAInReal[1]);
                }
                pa.onAccelChanged(meanAInReal);
                lastTimeSend=currentT;
                counter=0;
                meanAInReal[0]=0;
                meanAInReal[1]=0;
            }
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