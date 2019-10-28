package com.example.bleproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner blueToothLeScanner;
    private static int REQUEST_ENABLE_BT = 1;
    private boolean mScanning;
    private Handler handler;
    private BluetoothGatt bluetoothGatt;
    private static UUID BULB_CHARACTERISTIC_UUID = UUID.fromString("FB959362-F26E-43A9-927C-7E17D8FB2D8D");
    private static UUID TEMPERATURE_CHARACTERISTIC_UUID = UUID.fromString("0CED9345-B31F-457D-A6A2-B3DB9B03E39A");
    private static UUID BEEP_CHARACTERISTIC_UUID = UUID.fromString("EC958823-F26E-43A9-927C-7E17D8F32A90");
    //private static UUID SERIVCE_UUID = UUID.fromString("dffa041d-9e1b-6f38-3035-63bdd4024a4d");
    private UUID[] SERVICE_UUID_LIST;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002934-0000-1000-8000-00805f9b34fb");
    private static byte ZERO = 0;
    private static byte ONE = 1;
    private static int BULB_TYPE = 1;
    private static int BEEP_TYPE = 2;
    private static final long SCAN_PERIOD = 10000;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Blue BLE");

        progressBar = findViewById(R.id.progressBar);
        SERVICE_UUID_LIST = new UUID[1];
        SERVICE_UUID_LIST[0] = UUID.fromString("dffa041d-9e1b-6f38-3035-63bdd4024a4d");

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access.");
                builder.setMessage("Please grant location access.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        findViewById(R.id.buttonOn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] value = new byte[1];
                value[0] = ONE;
                writeCharacteristic(value, BULB_TYPE);
            }
        });

        findViewById(R.id.buttonOff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] value = new byte[1];
                value[0] = ZERO;
                writeCharacteristic(value, BULB_TYPE);
            }
        });

        findViewById(R.id.ImageBeepOn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] value = new byte[1];
                value[0] = ONE;
                writeCharacteristic(value, BEEP_TYPE);
            }
        });
        findViewById(R.id.ImageBeepOff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] value = new byte[1];
                value[0] = ZERO;
                writeCharacteristic(value, BEEP_TYPE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void scanLeDevice(boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler = new Handler(getApplicationContext().getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothAdapter.startLeScan(SERVICE_UUID_LIST, leScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    connectToDevice(device);
                }
            };

    public void connectToDevice(final BluetoothDevice device) {
        if (bluetoothGatt == null) {
            if (Build.VERSION.SDK_INT >= 23) {
                bluetoothGatt = device.connectGatt(getApplicationContext(), false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            } else {
                bluetoothGatt = device.connectGatt(getApplicationContext(), false, gattCallback);
            }
            scanLeDevice(false);// will stop after first device detection
        } else {
            scanLeDevice(false);
        }

    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    BluetoothGatt b = gatt;
                    b.discoverServices(); //this invoke the onServicesDiscovered method,
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                    scanLeDevice(true);
                    break;
                default:
                    Log.e("thedemo", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // BluetoothGattCharacteristic characteristic = null;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service =
                        gatt.getService(SERVICE_UUID_LIST[0]);
                BluetoothGattCharacteristic temperatureCharacteristic =
                        service.getCharacteristic(TEMPERATURE_CHARACTERISTIC_UUID);


                gatt.setCharacteristicNotification(temperatureCharacteristic, true);
             /*   for (BluetoothGattDescriptor descriptor:temperatureCharacteristic.getDescriptors()){
                    Log.e("thedemodesrip", "BluetoothGattDescriptor: "+descriptor.getUuid().toString());
                }
                BluetoothGattDescriptor descriptor = temperatureCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.readDescriptor(descriptor);*/

            } else if (status == BluetoothProfile.STATE_DISCONNECTED) {
                Toast.makeText(MainActivity.this, "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID_LIST[0]).getCharacteristic(TEMPERATURE_CHARACTERISTIC_UUID);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView temp = findViewById(R.id.textViewTemperature);
                    temp.setText(characteristic.getStringValue(0) + " F");
                }
            });
        }
    };

    public boolean writeCharacteristic(byte value[], int type) {
        //check mBluetoothGatt is available
        if (bluetoothGatt == null) {
            Log.e("thedemo in write", "lost connection");
            return false;
        }
        BluetoothGattService Service = bluetoothGatt.getService(SERVICE_UUID_LIST[0]);
        if (Service == null) {
            return false;
        }
        BluetoothGattCharacteristic charac1 = null;
        boolean status1 = false;

        if (type == 1) {
            charac1 = Service.getCharacteristic(BULB_CHARACTERISTIC_UUID);
            charac1.setValue(value);
            status1 = bluetoothGatt.writeCharacteristic(charac1);
        } else if (type == 2) {

            charac1 = Service.getCharacteristic(BEEP_CHARACTERISTIC_UUID);
            charac1.setValue(value);
            status1 = bluetoothGatt.writeCharacteristic(charac1);
        }
        if (charac1 == null) {
            return false;
        }
        return status1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("thedemo", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover BLE.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {
            progressBar.setVisibility(View.VISIBLE);
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (bluetoothGatt == null) {
            super.onDestroy();
            return;
        }
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;

        super.onDestroy();
    }

}
