package com.itdimension.accelerometertest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.achartengine.*;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class AccelerometerTest extends Activity implements SensorEventListener {
	
	SensorManager mSensorManager;
	Sensor mAccelerometerSensor;
	Sensor mMagneticFieldSensor;
	
	TextView mForceValueText;
	TextView mXValueText;
	TextView mYValueText;
	TextView mZValueText;
	
	double margins[] = {0, 0};
	
	Button mStartButton;
	Button mShowButton;
	
	List<List<Double>> mValues;
	boolean mIsRecording = false;
	
	OnClickListener mStartButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mIsRecording = !mIsRecording;
			if(mIsRecording) {
				mValues.get(SensorManager.DATA_X).clear();
				mValues.get(SensorManager.DATA_Y).clear();
				mValues.get(SensorManager.DATA_Z).clear();
				margins[0] = 0;
				margins[1] = 0;
			}
		}
	};
	
	OnClickListener mShowButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			try
			{
				Intent intent = getChartIntent();
				startActivity(intent);
			}
			catch (Exception e) {
				new AlertDialog.Builder(AccelerometerTest.this)
					.setTitle("Error")
					.setMessage(e.getMessage())
					.create()
					.show();
			}
			
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mValues = new ArrayList<List<Double>>();
        mValues.add(new ArrayList<Double>());
        mValues.add(new ArrayList<Double>());
        mValues.add(new ArrayList<Double>());
        
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        if(sensors.size() > 0)
        {
        	for (Sensor sensor : sensors) {
        		switch(sensor.getType())
        		{
        		case Sensor.TYPE_ACCELEROMETER:
        			if(mAccelerometerSensor == null) mAccelerometerSensor = sensor;
        			break;
        		case Sensor.TYPE_MAGNETIC_FIELD:
        			if(mMagneticFieldSensor == null) mMagneticFieldSensor = sensor;
        			break;
        		default:
        			break;
        		}
			}
        }
        
        mForceValueText = (TextView)findViewById(R.id.value_force);
        mXValueText = (TextView)findViewById(R.id.value_x);
        mYValueText = (TextView)findViewById(R.id.value_y);
        mZValueText = (TextView)findViewById(R.id.value_z);
        
        mStartButton = (Button)findViewById(R.id.button_start);
        mShowButton = (Button)findViewById(R.id.button_show);
        
        mStartButton.setOnClickListener(mStartButtonListener);
        mShowButton.setOnClickListener(mShowButtonListener);
    }
    
    @Override
    protected void onPause() {
    	mSensorManager.unregisterListener(this);
    	super.onPause();
    	
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    	mSensorManager.registerListener(this, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {	
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float [] values = event.values;
		switch(event.sensor.getType())
		{
		case Sensor.TYPE_ACCELEROMETER:
			{
				if(mIsRecording)
				{
					recordSensorValue(event);
				}
				
				mXValueText.setText(String.format("%1.3f", event.values[SensorManager.DATA_X]));
				mYValueText.setText(String.format("%1.3f", event.values[SensorManager.DATA_Y]));
				mZValueText.setText(String.format("%1.3f", event.values[SensorManager.DATA_Z]));
	            
	            double totalForce = 0.0f;
	            totalForce += Math.pow(values[SensorManager.DATA_X]/SensorManager.GRAVITY_EARTH, 2.0);
	            totalForce += Math.pow(values[SensorManager.DATA_Y]/SensorManager.GRAVITY_EARTH, 2.0);
	            totalForce += Math.pow(values[SensorManager.DATA_Z]/SensorManager.GRAVITY_EARTH, 2.0);
	            totalForce = Math.sqrt(totalForce);
	            mForceValueText.setText(String.format("%1.3f", totalForce));
			}
			break;
		}
	}

	private void recordSensorValue(SensorEvent event) {
		double value;
		for(int i = SensorManager.DATA_X; i <= SensorManager.DATA_Z; i++)
		{
			value = (double)event.values[i];
			margins[0] = Math.min(margins[0], value);
			margins[1] = Math.max(margins[1], value);
			mValues.get(i).add(value);
		}
	}
	
	Intent getChartIntent() {
		int [] colors = new int[] { Color.RED, Color.GREEN, Color.BLUE };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT, PointStyle.POINT, PointStyle.POINT };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
	    setChartSettings(renderer, "Sensor Values", "Index", "Value", 
	    		0, 
	    		mValues.get(SensorManager.DATA_X).size(), 
	    		margins[0] * 1.5,
	    		margins[1] * 1.5,
	        Color.GRAY, Color.LTGRAY);
		return ChartFactory.getLineChartIntent(this, buildDataset(), renderer);
	}
	
	protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
		      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
		      int labelsColor) {
		    renderer.setChartTitle(title);
		    renderer.setXTitle(xTitle);
		    renderer.setYTitle(yTitle);
		    renderer.setXAxisMin(xMin);
		    renderer.setXAxisMax(xMax);
		    renderer.setYAxisMin(yMin);
		    renderer.setYAxisMax(yMax);
		    renderer.setAxesColor(axesColor);
		    renderer.setLabelsColor(labelsColor);
		  }
	
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    int length = colors.length;
	    for (int i = 0; i < length; i++) {
	      XYSeriesRenderer r = new XYSeriesRenderer();
	      r.setColor(colors[i]);
	      r.setPointStyle(styles[i]);
	      renderer.addSeriesRenderer(r);
	    }
	    return renderer;
	  }
	
	XYMultipleSeriesDataset buildDataset() {
		XYMultipleSeriesDataset result = new XYMultipleSeriesDataset();
		XYSeries xSeries = new XYSeries("X");
		XYSeries ySeries = new XYSeries("Y");
		XYSeries zSeries = new XYSeries("Z");
		
		int count = mValues.get(SensorManager.DATA_X).size();
		for(int i = 0; i < count; i++)
		{
			xSeries.add(i, mValues.get(SensorManager.DATA_X).get(i));
			ySeries.add(i, mValues.get(SensorManager.DATA_Y).get(i));
			zSeries.add(i, mValues.get(SensorManager.DATA_Z).get(i));
		}
		
		result.addSeries(xSeries);
		result.addSeries(ySeries);
		result.addSeries(zSeries);
		
		return result;
	}
}