package com.mycompany.myfirstindoorsapp;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Work with Kalmant filter: creates specific matrixes, gives estimation
 */

public class Kalman {
    long dt=2; // time of new accel (2 millisec)
    double accelNoise;
    double measurementNoise;
    KalmanFilter filter;
    private final Object lock = new Object();


    Kalman(double accelNoise, double measurementNoise, RealVector x) {
        // x = [ 0 0 ] initial coordinates. from example:
        //RealVector x = new ArrayRealVector(new double[] { 0, 0 });
        this.accelNoise = accelNoise;
        this.measurementNoise = measurementNoise;
    /*        A - state transition matrix
        B - control input matrix
        H - measurement matrix
        Q - process noise covariance matrix
        R - measurement noise covariance matrix
        P - error covariance matrix*/

 /*   A
    1 0 dt 0
    0 1 0  dt
    0 0 1  0
    0 0 0  1*/
        RealMatrix A = new Array2DRowRealMatrix(new double[][]{{1, 0, dt, 0},
                {0, 1, 0, dt},
                {0, 0, 1, 0},
                {0, 0, 0, 1}});
/*    B
    (dt^2)/2 0
    0 (dt^2)/2
    dt 0
    0 dt*/
        RealMatrix B = new Array2DRowRealMatrix(new double[][]{{Math.pow(dt, 2d) / 2d, 0},
                {0, (dt ^ 2) / 2},
                {dt, 0,},
                {0, dt,}});
/*    H
    1 0 0 0
    0 1 0 0*/
        //TODO check how mesurement phase is performed
        RealMatrix H = new Array2DRowRealMatrix(new double[][]{{1, 0, 0 ,0},
                {0, 1, 0, 0}});

        //TODO Q matrix (cov)
        // это матожидание квадрата разностей
        //Q=accelNoise*B*BT
        RealMatrix Q = B.multiply(B.transpose()).scalarMultiply(accelNoise * accelNoise);
        // P0 = [ 10 0 ]
//      [ 0 10 ]
        //TODO changed dim of P0
        RealMatrix P0 = new Array2DRowRealMatrix(new double[][]{{10,0,0,0},
                {0, 10, 0, 0},
                {0, 0, 10, 0},
                {0, 0, 0, 10}});

        // R = [ measurementNoise^2 ]
        RealMatrix R = new Array2DRowRealMatrix(new double[][]{{Math.pow(measurementNoise, 2),
                0},
                {0, Math.pow(measurementNoise, 2)}});


        ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        filter = new KalmanFilter(pm, mm);
    }

    void predict(RealVector u) //RealVector u = new ArrayRealVector(new double[][]{{ax}, {ay}});
    // u=[ax, ay]
    {
        synchronized (lock) {
            filter.predict(u);
        }
    }

    void correct(RealVector z) {

        synchronized (lock) {
            filter.correct(z);
        }
    }

    RealVector getStateEstimationVector() {

        synchronized (lock) {
            return filter.getStateEstimationVector();
        }
    }
}
