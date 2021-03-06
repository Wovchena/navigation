package diplomar.myacselerometertest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static java.lang.Math.sqrt;

public class AccelerometerTest extends AppCompatActivity implements SensorEventListener {
    SensorManager mSensorManager;
    Sensor mAccelerometerSensor;
    Sensor mMagneticFieldSensor;

    TextView mForceValueText;
    TextView mXValueText;
    TextView mYValueText;
    TextView mZValueText;

    double mean = 0;
    double d = 0;
    int c = 0;
    int c2 = 0;
    int w8=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_test);

        mForceValueText = (TextView) findViewById(R.id.value_force);
        mXValueText = (TextView) findViewById(R.id.value_x);
        mYValueText = (TextView) findViewById(R.id.value_y);
        mZValueText = (TextView) findViewById(R.id.value_z);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        if (sensors.size() > 0) {
            for (Sensor sensor : sensors) {
                switch (sensor.getType()) {
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        Log.d("Acsel", "fire");
                        if (mAccelerometerSensor == null) mAccelerometerSensor = sensor;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        if (mMagneticFieldSensor == null) mMagneticFieldSensor = sensor;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("Acsel", "iceee");
        float[] values = event.values;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                if (w8>=2000) {
                    if (c < 1000) {
                        // Здесь можно обрабатывать данные от сенсора
                        mean += sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event
                                .values[2] * event.values[2]);
                        c++;
                    }
                    if (c == 1000) {
                        mean = mean / (double) c;
                        Toast.makeText(this, "" + mean, Toast
                                .LENGTH_LONG).show();
                        c++;
                        Log.d("meanmean", "" + mean);
                    }

                    if (c >= 1000) {
                        d += (mean - sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event
                                .values[2] * event.values[2])) * (mean - sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event
                                .values[2] * event.values[2]));
                        c2++;
                        if (c2 == 1000) {
                            d = sqrt(d / (double) c2);
                            Toast.makeText(this, "" + d, Toast
                                    .LENGTH_LONG).show();
                            Log.d("dispdisp", "" + d);
                            onPause();
                        }
                    }
                }
                else {w8++;}
                mXValueText.setText(String.format("%1.3f",
                        event.values[0]));
                mYValueText.setText(String.format("%1.3f",
                        event.values[1]));
                mZValueText.setText(String.format("%1.3f",
                        event.values[2]));

                double totalForce = 0.0f;
                totalForce += Math.pow(
                        values[SensorManager.DATA_X] / SensorManager.GRAVITY_EARTH, 2.0);
                totalForce += Math.pow(
                        values[SensorManager.DATA_Y] / SensorManager.GRAVITY_EARTH, 2.0);
                totalForce += Math.pow(
                        values[SensorManager.DATA_Z] / SensorManager.GRAVITY_EARTH, 2.0);
                totalForce = sqrt(totalForce);
                mForceValueText.setText(String.format("%1.3f", totalForce));
            }
            break;
        }
    }
}