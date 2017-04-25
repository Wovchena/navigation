package com.mycompany.myfirstindoorsapp;

import android.os.SystemClock;

import com.customlbs.shared.Coordinate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wov on 01.03.17.
 */

public class PositionCounter {
    HashMap<Coordinate, Long> storendCoords = new HashMap<>();
    static long time = SystemClock.uptimeMillis();
    static long newDifference = 0;
    static Coordinate oldCoord = new Coordinate(0,0,0);
    long newPosition(Coordinate newPosition)
    {
        newDifference = SystemClock.uptimeMillis()-time;
        storendCoords.put(oldCoord, newDifference);
        oldCoord=newPosition;
        time=SystemClock.uptimeMillis();
        return newDifference;
    }

    void getSummury()
    {
        for (Map.Entry i : storendCoords.entrySet());

    }
}
