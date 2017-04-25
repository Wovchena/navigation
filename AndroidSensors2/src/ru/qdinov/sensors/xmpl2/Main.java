package ru.qdinov.sensors.xmpl2;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class Main extends Activity implements SensorEventListener{
	
	private SensorManager msensorManager;
	
    private float[] rotationMatrix;
    private float[] accelData;
    private float[] magnetData;
	private float[] OrientationData;
	
	public TextView xyView;
	public TextView xzView;
	public TextView zyView;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        msensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        
        rotationMatrix = new float[16];
        accelData = new float[3];
        magnetData = new float[3];
        OrientationData = new float[3];
        
        xyView = (TextView) findViewById(R.id.xyValue);  //
        xzView = (TextView) findViewById(R.id.xzValue);  // Ќаши текстовые пол€ дл€ вывода показаний
        zyView = (TextView) findViewById(R.id.zyValue);  //
        
        setContentView(R.layout.main);
        
        
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	msensorManager.registerListener(this, msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI );
    	msensorManager.registerListener(this, msensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI );
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        msensorManager.unregisterListener(this);
    }
            

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		loadNewSensorData(event);
        SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData);
        SensorManager.getOrientation (rotationMatrix, OrientationData);
        
        if((xyView==null)||(xzView==null)||(zyView==null)){
        	xyView = (TextView) findViewById(R.id.xyValue);  //
            xzView = (TextView) findViewById(R.id.xzValue);  // Ќаши текстовые пол€ дл€ вывода показаний
            zyView = (TextView) findViewById(R.id.zyValue);  //
        }
        
        xyView.setText(String.valueOf(Math.round(Math.toDegrees(OrientationData[0]))));
        xzView.setText(String.valueOf(Math.round(Math.toDegrees(OrientationData[1]))));
        zyView.setText(String.valueOf(Math.round(Math.toDegrees(OrientationData[2]))));
	}
	
	private void loadNewSensorData(SensorEvent event) {
        
        final int type = event.sensor.getType();
       
        if (type == Sensor.TYPE_ACCELEROMETER) {
                accelData = event.values.clone();
        }
       
        if (type == Sensor.TYPE_MAGNETIC_FIELD) {
                magnetData = event.values.clone();
        }
	}
	
}