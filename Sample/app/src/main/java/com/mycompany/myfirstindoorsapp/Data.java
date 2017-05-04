package com.mycompany.myfirstindoorsapp;

import com.customlbs.shared.Coordinate;

import java.sql.Time;
import java.util.Iterator;
import java.util.Vector;

import static java.lang.Math.sqrt;

/**
 * Prints info into file or on fragment
 */


public class Data {
    public Vector<TimeAndPos> indoors;
    public Vector<TimeAndPos> filter;
    Coordinate initialCoord = null;
    Coordinate endCoord = null;
    double indoorsDeviationSumm = 0;
    double filterDeviationSumm = 0;
    long initialTime = 0;
    long endTime = 0;

    Data(Coordinate c) {
        initialCoord = c;
        indoors = new Vector<>(1000);
        filter = new Vector<>(1000);
    }

    Data(Coordinate c1, Coordinate c2, long t) {
        initialCoord = c1;
        endCoord = c2;
        indoors = new Vector<>(1000);
        filter = new Vector<>(1000);
        initialTime = t;
    }

    double[] getRealCoord(long t) {
        double[] c = new double[2];
        if (endCoord == null) {
            c[0] = initialCoord.x;
            c[1] = initialCoord.y;
            return c;
        }
        long timeSpent = t - initialTime;
        c[0] = initialCoord.x + (endCoord.x - initialCoord.x) * (double) timeSpent / (double)
                (endTime - initialTime);
        c[1] = initialCoord.y + (endCoord.y - initialCoord.y) * (double) timeSpent / (double)
                (endTime - initialTime);
        return c;

    }

    void addToIndoors(long t, Coordinate c) {
        indoors.add(new TimeAndPos(t, c));
    }

    void addToFilter(long t, Coordinate c) {
        filter.add(new TimeAndPos(t, c));
    }

    double[] calc(long endTime) {
        this.endTime = endTime;
        for (TimeAndPos i : indoors) {
            double[] RealCoordForI = getRealCoord(i.t);
            indoorsDeviationSumm += Math.pow(i.c.x - RealCoordForI[0], 2) + Math.pow(i.c
                    .y - RealCoordForI[1], 2);
        }
        double[] res = new double[2];
        res[0] = sqrt(indoorsDeviationSumm / indoors.size())/1000;//in meters

        for (TimeAndPos f : filter) {
            double[] RealCoordForF = getRealCoord(f.t);
            filterDeviationSumm += Math.pow(f.c.x - RealCoordForF[0], 2) + Math.pow(f.c.y -
                    RealCoordForF[1], 2);
        }
        res[1]=sqrt(filterDeviationSumm/filter.size())/1000; // in meters
        //TODO some tests are needed
        return res; // среднеквадратичное отклонение для indoors and Kalman
    }
}
