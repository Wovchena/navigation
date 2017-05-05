package diplomar.myorientationtest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;

    public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager msensorManager;
    // A = [ 1 ]
    RealMatrix A = new Array2DRowRealMatrix(new double[] { 1d });


    private float[] rotationMatrix;
    private float[] accelData;
    private float[] magnetData;
    private float[] OrientationData;
        float [] myrotationMatrix=new float[9];

    public TextView xyView;
    public TextView xzView;
    public TextView zyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(diplomar.myorientationtest.R.layout.activity_main);

        msensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        rotationMatrix = new float[16];
        accelData = new float[3];
        magnetData = new float[3];
        OrientationData = new float[3];


        xyView = (TextView) findViewById(diplomar.myorientationtest.R.id.xyValue);  //
        xzView = (TextView) findViewById(diplomar.myorientationtest.R.id.xzValue);  // ���� ��������� ���� ��� ������ ���������
        zyView = (TextView) findViewById(diplomar.myorientationtest.R.id.zyValue);  //
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
        double[][] doubleRotationMatrix=new double[3][3];
        loadNewSensorData(event);
        SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData);
        SensorManager.getOrientation (rotationMatrix, OrientationData);
        SensorManager.getRotationMatrix(myrotationMatrix, null, accelData, magnetData);
        int c=0;
        for (int i=0; i<3; i++)
        {
            for (int j=0; j<3; j++) {
                doubleRotationMatrix[i][j] = myrotationMatrix[c];
                c++;
            }
        }

        double[] vector=new double[3];
        vector[0]=100;
        vector[1]=0;
        vector[2]=0;
        vector=(new Array2DRowRealMatrix(doubleRotationMatrix)).operate(vector);
        /*aInReal[0]=aInBasic[0]*Math.cos(rotation)-aInBasic[1]*Math.sin(rotation);
        aInReal[0]=aInReal[0]*1000;
        aInReal[1]=aInBasic[0]*Math.sin(rotation)+aInBasic[1]*Math.cos(rotation);*/
        Log.d("vector", ""+vector[0]+" | "+vector[1]+" | "+vector[2]);


        xyView.setText(String.valueOf(Math.round(Math.toDegrees(OrientationData[0]))));
        zyView.setText(String.valueOf(Math.round(Math.toDegrees(OrientationData[1]))));
        xzView.setText(String.valueOf(Math.round(Math.toDegrees(OrientationData[2]))));
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
