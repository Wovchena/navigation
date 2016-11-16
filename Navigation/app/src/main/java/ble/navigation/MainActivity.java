package ble.navigation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final static int REQUEST_ENABLE_BT=1;
    ArrayAdapter<AFMBeacon> adapter;
    ArrayList<AFMBeacon> mDevices;
    BluetoothAdapter mBluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lvMain = (ListView) findViewById(R.id.lvMain);
        mDevices=new ArrayList<AFMBeacon>();
        adapter = new ArrayAdapter<AFMBeacon>(this, android.R.layout.simple_list_item_1, mDevices);
        lvMain.setAdapter(adapter);
        scanStart();
    }
    public void scanStart() {
// If bluetooth is OFF ask from user to turn it ON
// otherwise start scan
        BluetoothManager bluetoothManager=(BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        mBluetoothAdapter= bluetoothManager.getAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            mBluetoothAdapter.startLeScan(scanCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mBluetoothAdapter.startLeScan(scanCallback);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
            if (resultCode == RESULT_CANCELED) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }
    }

    // Callback for scanning
    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[]
                scanRecord) {
            for (AFMBeacon dev:mDevices){
                if (dev.getAdress().equals(device.getAddress()))
                {
                    mDevices.remove(dev);
                    break;
                }
            }
            final int MAJOR_UPPER_INDEX = 25;
            final int BIT_MASK = 0xff;
            final int SHIFT_MASK = 0x100;
            final int MAJOR_LOWER_INDEX = 26;
            final int MINOR_UPPER_INDEX = 27;
            final int MINOR_LOWER_INDEX = 28;

            final int major = (scanRecord[MAJOR_UPPER_INDEX] & BIT_MASK)
                    * SHIFT_MASK + (scanRecord[MAJOR_LOWER_INDEX] & BIT_MASK);
            final int minor = (scanRecord[MINOR_UPPER_INDEX] & BIT_MASK)
                    * SHIFT_MASK + (scanRecord[MINOR_LOWER_INDEX] & BIT_MASK);
            final int tmprssi = rssi;
            mDevices.add(new AFMBeacon(device.getName(), device.getAddress(), major, minor, rssi));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };
}

