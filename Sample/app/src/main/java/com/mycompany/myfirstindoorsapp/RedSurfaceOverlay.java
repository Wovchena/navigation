package com.mycompany.myfirstindoorsapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;

import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurfaceOverlay;
import com.customlbs.surface.library.SurfacePainterConfiguration;
import com.customlbs.surface.library.SurfaceState;



/**
 * Created by wov on 27.01.17.
 */

public class RedSurfaceOverlay implements IndoorsSurfaceOverlay {
    Coordinate mapPoint;
    public RedSurfaceOverlay (){
    }
    public RedSurfaceOverlay (Coordinate mapPoint)
    {
        this.mapPoint=mapPoint;
    }

    @Override
    public void initialize(SurfacePainterConfiguration surfacePainterConfiguration) {
        //SurfacePainterConfiguration.PaintConfiguration.getDimensionPixelSize();

    }

    @Override
    public void paint(Canvas canvas, SurfaceState surfaceState) {
/*        Log.i("coord", "mapPoint.x="+mapPoint.x);
        Log.i("coord", "mapPoint.y="+mapPoint.y);*/

        int mPx=mapPoint.x;
        int mPy=mapPoint.y;

        int x =  (int)java.lang.Math.round((mPx*surfaceState.getScaledOverlaySize(1)*surfaceState.mapZoomFactor*surfaceState.mapZoomFactor));



        int y =  (int)java.lang.Math.round((mPy*surfaceState.getScaledOverlaySize(1)*surfaceState.mapZoomFactor*surfaceState.mapZoomFactor));
        x=x+(int)java.lang.Math.round(surfaceState.mapX);
        y=y+(int)java.lang.Math.round(surfaceState.mapY);
/*        Log.i("coord", "x="+x);
        Log.i("coord", "y="+y);
        Log.i("coord", ""+surfaceState.getScaledOverlaySize(1)); // размер объекта в 1мм
        Log.i("coord", "surfaceState.mapX="+surfaceState.mapX); // смещение карты
        Log.i("coord", ""+surfaceState.mapZoomFactor);*/

        //int width = (int)java.lang.Math.round(10/surfaceState.getScaledOverlaySize(10));
        //int height = (int)java.lang.Math.round(10/surfaceState.getScaledOverlaySize(10)); круг всегда одного размера на экране
        int width=20;
        int height=20;
/*        Log.i("coord", "scaled1="+(surfaceState.getScaledOverlaySize(1)));
        Log.i("coord", "scaled2="+(surfaceState.getScaledOverlaySize(2)));
        Log.i("coord", "Zoom="+surfaceState.mapZoomFactor);
        Log.i("coord", "width="+width);*/


        ShapeDrawable mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(Color.RED);
        mDrawable.setBounds(x, y, x + width, y + height);
        mDrawable.draw(canvas);
        //Log.i("coord", ""+mapPoint.score);
    }


    @Override
    public void destroy() {

    }
}
