package com.mycompany.myfirstindoorsapp;

import com.customlbs.shared.Coordinate;

import java.util.Vector;

/**
 * Prints info into file or on fragment
 */


public class Data {
    public Vector<TimeAndPos> indoors;
    public Vector<TimeAndPos> filter;
    Coordinate initialCoord=null;
    Coordinate endCoord=null;
    Data (Coordinate c)
    {
        initialCoord=c;
        indoors=new Vector<>(1000);
        filter=new Vector<>(1000);
    }

    Data (Coordinate c1, Coordinate c2)
    {
        initialCoord=c1;
        endCoord=c2;
        indoors=new Vector<>(1000);
        filter=new Vector<>(1000);
    }

    void addToIndoors (long t, Coordinate c)
    {
        indoors.add(new TimeAndPos(t, c));
    }
    void addToFilter (long t, Coordinate c)
    {
        filter.add(new TimeAndPos(t, c));
    }
    void calc ()
    {
        //TODO mesure errors
    }
}
