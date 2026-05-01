package com.sz.cp2102;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity1 extends AppCompatActivity {
    private TextView txtble;
    private ListView listView;
    private ResultAdapter mResultAdapter;
    private EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setOperateTimeout(5000);
        listView = findViewById(R.id.listView);
        mResultAdapter = new ResultAdapter(this);
        listView.setAdapter(mResultAdapter);
        editText = findViewById(R.id.editText);
        txtble = findViewById(R.id.txtble);
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setble();
            }
        });
        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mybleDevice == null) {
                    Toast.makeText(MainActivity1.this, "请搜索蓝牙设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                link(mybleDevice);
            }
        });
        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                send();
            }
        });
//        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//            @Override
//            public void onClick(View v) {
//                write();
//            }
//        });

        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                send3();
            }
        });
        findViewById(R.id.btn6).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                send6();
            }
        });
        findViewById(R.id.btn7).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                send9();
            }
        });
        findViewById(R.id.btn8).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().length() == 0) {
                    Toast.makeText(MainActivity1.this, getResources().getText(R.string.instructions), Toast.LENGTH_SHORT).show();
                } else {
                    send7(editText.getText().toString().trim());
                }
            }
        });
        chechLocation();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward the result to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void chechLocation() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Permission already granted
            // ...

        } else {
            // Permission is missing; request it now
            // ...
            EasyPermissions.requestPermissions(this, getResources().getText(R.string.applyBlue  ).toString(),
                    1001, perms);
        }

    }

//    @Override
//    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
//        // Some permissions were granted
//        Toast.makeText(this, "Allowed", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
//        // Some permissions were denied
//        Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onPermissionsDenied(int requestCode, List<String> perms) {
//        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
//            new AppSettingsDialog.Builder(this).build().show();
//            //Show a customizable dialog
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setBle() {
        if (BleManager.getInstance().isSupportBle()) {
            Log.e("setble", "支持蓝牙");
            if (BleManager.getInstance().isBlueEnable()) {
                Log.e("setble", "蓝牙可用");
            } else {
                BleManager.getInstance().enableBluetooth();
            }
        } else {

        }
    }

    public void setble() {
//               .setServiceUuids(serviceUuids)      // Only scan devices advertising the specified services; optional
//                .setDeviceName(true, names)         // Only scan devices with the specified advertised name; optional
//                .setDeviceMac(mac)                  // Only scan the specified MAC address; optional
//                .setAutoConnect(isAutoConnect)      // autoConnect parameter for the connection; optional, defaults to false
        Log.e("setble", "setble");
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()

                .setScanTimeOut(10000)              // Scan timeout; optional, defaults to 10 seconds
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);

        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                Log.e("setble33", bleDevice.getMac());
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                Log.e("setble22", bleDevice.getMac());
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                for (BleDevice d : scanResultList) {
                    Log.e("setble99:", d.getMac());
                    if (d.getName() != null) {
                        Log.e("setble99Name:", d.getName());
                        mResultAdapter.addResult("发现蓝牙设备:" + d.getName());
                        mResultAdapter.notifyDataSetChanged();
                        if (d.getName().equalsIgnoreCase("AC696X_1(BLE)")) {
                            txtble.setText("发现蓝牙设备");
                            mybleDevice = d;
                        }
                    } else {
                        mResultAdapter.addResult("发现蓝牙设备:" + d.getMac());
                    }
                }
            }
        });
    }

    public String UUID_KEY_DATA = "00002a00-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR1 = "0000ae01-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR2 = "0000ae02-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR3 = "0000ae03-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR4 = "0000ae04-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR5 = "0000ae05-0000-1000-8000-00805f9b34fb";
    public String UUID_CHAR6 = "0000ae10-0000-1000-8000-00805f9b34fb";
    public String UUID_HERATRATE = "0000ae3b-0000-1000-8000-00805f9b34fb";
    public String UUID_TEMPERATURE = "0000ae3c-0000-1000-8000-00805f9b34fb";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void link(BleDevice bleDevice) {
        mResultAdapter.clear();
        mResultAdapter.notifyDataSetChanged();
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {

                Log.e("setble88:", "开始连接");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

                Log.e("setble88:", "连接失败");
            }


            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.e("setble88:", "连接成功");
                Log.e("setble88:", "状态码:" + status);
                txtble.setText("蓝牙连接成功");
                commet();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.e("setble8811:", "状态码:" + status);
                Log.e("setble8811:", "状态码:" + bleDevice.toString());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void commet() {
        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(mybleDevice);
        List<BluetoothGattService> serviceList = gatt.getServices();
        for (BluetoothGattService service : serviceList) {
            UUID uuid_service = service.getUuid();
            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristicList) {
                UUID uuid_chara = characteristic.getUuid();
                Log.e("setble88664411", uuid_chara.toString());
                if (uuid_chara.toString().equals(UUID_CHAR1)) {
                    characteristic1 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 1);
                }
                if (uuid_chara.toString().equals(UUID_CHAR2)) {
                    characteristic2 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 2);
                }

                if (uuid_chara.toString().equals(UUID_CHAR3)) {
                    characteristic3 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 3);
                }

                if (uuid_chara.toString().equals(UUID_CHAR4)) {
                    characteristic4 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 4);
                }

                if (uuid_chara.toString().equals(UUID_CHAR5)) {
                    characteristic5 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 5);
                }

                if (uuid_chara.toString().equals(UUID_CHAR6)) {
                    characteristic6 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 6);
                }

                if (uuid_chara.toString().equals(UUID_HERATRATE)) {
                    characteristic7 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 7);
                }
                if (uuid_chara.toString().equals(UUID_TEMPERATURE)) {
                    characteristic8 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 8);
                }
                if (uuid_chara.toString().equals(UUID_KEY_DATA)) {
                    characteristic9 = characteristic;
                    list.add(characteristic);
                    ble_connect(mybleDevice, 8);
                }
//                Log.e("characteristic",uuid_chara.);
                int charaProp = characteristic.getProperties();
            }
        }
    }

    public ArrayList<BluetoothGattCharacteristic> list = new ArrayList<BluetoothGattCharacteristic>();
    public BluetoothGattCharacteristic characteristic1;
    public BluetoothGattCharacteristic characteristic2;
    public BluetoothGattCharacteristic characteristic3;
    public BluetoothGattCharacteristic characteristic4;
    public BluetoothGattCharacteristic characteristic5;
    public BluetoothGattCharacteristic characteristic6;

    public BluetoothGattCharacteristic characteristic7;
    public BluetoothGattCharacteristic characteristic8;

    public BluetoothGattCharacteristic characteristic9;
    public BleDevice mybleDevice;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void ble_connect(BleDevice bleDevice, int type) {
        Log.e("setble88999", "ble_connect");
        if (type == 2)

            BleManager.getInstance().notify(
                    bleDevice,
                    characteristic2.getService().getUuid().toString(),
                    characteristic2.getUuid().toString(),
                    new BleNotifyCallback() {
                        @Override
                        public void onNotifySuccess() {
                            // Enabling notifications succeeded
                            Log.e("setble66633", "onNotifySuccess2");
                        }

                        @Override
                        public void onNotifyFailure(BleException exception) {
                            Log.e("setble666633", "onNotifyFailure2");
                            // Enabling notifications failed
                        }

                        @Override
                        public void onCharacteristicChanged(byte[] data) {
                            // Incoming device data appears here after notifications are enabled
                            Log.e("setble666332", HexUtil.formatHexString(data, true));

                            add("已接收" + HexUtil.formatHexString(data, true));
                        }
                    });
        if (type == 4)
            BleManager.getInstance().notify(
                    bleDevice,
                    characteristic4.getService().getUuid().toString(),
                    characteristic4.getUuid().toString(),
                    new BleNotifyCallback() {
                        @Override
                        public void onNotifySuccess() {
                            // Enabling notifications succeeded
                            Log.e("setble66633", "onNotifySuccess4");
                        }

                        @Override
                        public void onNotifyFailure(BleException exception) {
                            Log.e("setble666633", "onNotifyFailure4");
                            // Enabling notifications failed
                        }

                        @Override
                        public void onCharacteristicChanged(byte[] data) {
                            // Incoming device data appears here after notifications are enabled
                            Log.e("setble666334", HexUtil.formatHexString(data, true));
                        }
                    });

        if (type == 5)
            BleManager.getInstance().indicate(
                    bleDevice,
                    characteristic5.getService().getUuid().toString(),
                    characteristic5.getUuid().toString(),
                    new BleIndicateCallback() {
                        @Override
                        public void onIndicateSuccess() {
                            Log.e("setble66333333", "onIndicateSuccess5");
                            // Enabling notifications succeeded
                        }

                        @Override
                        public void onIndicateFailure(BleException exception) {
                            // Enabling notifications failed
                            Log.e("setble66333333", "exception5");
                        }

                        @Override
                        public void onCharacteristicChanged(byte[] data) {
                            // Incoming device data appears here after notifications are enabled
                            Log.e("setble663333335", HexUtil.formatHexString(data, true));
                        }
                    });
        try {

            if (type == 8)
                BleManager.getInstance().notify(
                        bleDevice,
                        characteristic8.getService().getUuid().toString(),
                        characteristic8.getUuid().toString(),
                        new BleNotifyCallback() {
                            @Override
                            public void onNotifySuccess() {
                                // Enabling notifications succeeded
                                Log.e("setble66633", "onNotifySuccess8");
                            }

                            @Override
                            public void onNotifyFailure(BleException exception) {
                                Log.e("setble666633", "onNotifyFailure8");
                                // Enabling notifications failed
                            }

                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                // Incoming device data appears here after notifications are enabled
                                Log.e("setble666338", HexUtil.formatHexString(data, true));
                            }
                        });

        } catch (Exception e) {

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void send() {
        if (mybleDevice == null) {
            Toast.makeText(MainActivity1.this, getResources().getString(R.string.placeBle), Toast.LENGTH_SHORT).show();
            return;
        }
        String hex = "5A810100000000000000000000000001";
        BleManager.getInstance().write(
                mybleDevice,
                characteristic1.getService().getUuid().toString(),
                characteristic1.getUuid().toString(),
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // Data sent to the device successfully. For packetized sends, use the callback data to inspect progress.
                        Log.e("setblesend", HexUtil.formatHexString(justWrite, true));
                        add("已发送" + HexUtil.formatHexString(justWrite, true));
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.e("setbleononReadFailure", "onWriteFailure");
                        // Failed to send data to the device
                    }
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void send9() {
        if (mybleDevice == null) {
            Toast.makeText(MainActivity1.this, getResources().getString(R.string.placeBle), Toast.LENGTH_SHORT).show();
            return;
        }
        String hex = "5A830100000000000000000000000001";
        BleManager.getInstance().write(
                mybleDevice,
                characteristic1.getService().getUuid().toString(),
                characteristic1.getUuid().toString(),
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // Data sent to the device successfully. For packetized sends, use the callback data to inspect progress.
                        Log.e("setblesend", HexUtil.formatHexString(justWrite, true));

                        add("已发送" + HexUtil.formatHexString(justWrite, true));
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.e("setbleononReadFailure", "onWriteFailure");
                        // Failed to send data to the device
                    }
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void send3() {
        if (mybleDevice == null) {
            Toast.makeText(MainActivity1.this, getResources().getString(R.string.placeBle), Toast.LENGTH_SHORT).show();
            return;
        }
        String hex = "5A810000000000000000000000000001";

        BleManager.getInstance().write(
                mybleDevice,
                characteristic1.getService().getUuid().toString(),
                characteristic1.getUuid().toString(),
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // Data sent to the device successfully. For packetized sends, use the callback data to inspect progress.
                        Log.e("setblesend", HexUtil.formatHexString(justWrite, true));
                        add("已发送" + HexUtil.formatHexString(justWrite, true));
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.e("setbleononReadFailure", "onWriteFailure");
                        // Failed to send data to the device
                    }
                });

    }

    private void add(String aa) {
        mResultAdapter.addResult(aa);
        mResultAdapter.notifyDataSetChanged();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void send6() {
        if (mybleDevice == null) {
            Toast.makeText(MainActivity1.this, getResources().getString(R.string.placeBle), Toast.LENGTH_SHORT).show();
            return;
        }
        String hex = "5A820100000000000000000000000001";

        BleManager.getInstance().write(
                mybleDevice,
                characteristic1.getService().getUuid().toString(),
                characteristic1.getUuid().toString(),
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // Data sent to the device successfully. For packetized sends, use the callback data to inspect progress.
                        Log.e("setblesend", HexUtil.formatHexString(justWrite, true));
                        add("已发送" + HexUtil.formatHexString(justWrite, true));
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.e("setbleononReadFailure", "onWriteFailure");
                        // Failed to send data to the device
                    }
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void send7(String hex) {
        if (mybleDevice == null) {
            Toast.makeText(MainActivity1.this, getResources().getString(R.string.placeBle), Toast.LENGTH_SHORT).show();
            return;
        }
        BleManager.getInstance().write(
                mybleDevice,
                characteristic1.getService().getUuid().toString(),
                characteristic1.getUuid().toString(),
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // Data sent to the device successfully. For packetized sends, use the callback data to inspect progress.
                        Log.e("setblesend", HexUtil.formatHexString(justWrite, true));
                        add("已发送" + HexUtil.formatHexString(justWrite, true));
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.e("setbleononReadFailure", "onWriteFailure");
                        // Failed to send data to the device
                    }
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void write() {
        for (int i = 0; i < list.size(); i++)
            BleManager.getInstance().read(
                    mybleDevice,
                    list.get(i).getService().getUuid().toString(),
                    list.get(i).getUuid().toString(),
                    new BleReadCallback() {
                        @Override
                        public void onReadSuccess(byte[] data) {
                            // Characteristic read succeeded
                            Log.e("setbleonReadSuccess", HexUtil.formatHexString(data, true));
                        }

                        @Override
                        public void onReadFailure(BleException exception) {
                            // Characteristic read failed
                            Log.e("setbleononReadFailure", "455454");
                        }
                    });
    }


    public static class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<String> characteristicList;

        public ResultAdapter(Context context) {
            this.context = context;
            characteristicList = new ArrayList<>();
        }

        public  void addResult(String service) {
            characteristicList.add(service);
        }

        void clear() {
            characteristicList.clear();
        }

        @Override
        public int getCount() {
            return characteristicList.size();
        }

        @Override
        public String getItem(int position) {
            if (position > characteristicList.size())
                return null;
            return characteristicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(context, R.layout.adapter_service, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
                holder.txt_uuid = (TextView) convertView.findViewById(R.id.txt_uuid);
                holder.txt_type = (TextView) convertView.findViewById(R.id.txt_type);
            }


            holder.txt_title.setText("数据:");
            holder.txt_uuid.setText(characteristicList.get(position));


            return convertView;
        }

        class ViewHolder {
            TextView txt_title;
            TextView txt_uuid;
            TextView txt_type;
        }
    }
}