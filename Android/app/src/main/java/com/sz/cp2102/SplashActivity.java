package com.sz.cp2102;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;

import android.util.Log;

import com.clj.fastble.utils.HexUtil;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.sz.cp2102.utils.TextUtils;

public class SplashActivity extends Activity {

    private static final int MAX_PAYLOAD_BYTES = 16;
    private static final String SEND_PREFIX = "AT+SENDB=01,02,";
    private static final String ACTION_USB_PERMISSION = "com.sz.cp2102.USB_PERMISSION";

    private String selectedShellId = "";  // To store the 4-digit ID
    private String selectedDestinationId = "";
    private String selectedSourceId = "";

    private UsbManager usbManager;
    private UsbSerialDriver usbDriver;
    private UsbDeviceConnection usbConnection;
    private boolean isLa66Connected = false;
    private boolean usbReceiverRegistered = false;
    private boolean waitingForUsbPermission = false;
    private String pendingCommandToSend = null;

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                waitingForUsbPermission = false;

                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (granted && device != null) {
                    Log.e("SplashUSB", "USB permission granted");

                    if (openLa66Connection()) {
                        if (pendingCommandToSend != null) {
                            String command = pendingCommandToSend;
                            pendingCommandToSend = null;
                            sendGeneratedAtCommand(command);
                        }
                    }
                } else {
                    pendingCommandToSend = null;
                    showToast("USB permission denied");
                    Log.e("SplashUSB", "USB permission denied");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.e("SplashUSB", "USB device attached");
                connectLa66IfNeeded();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.e("SplashUSB", "USB device detached");
                closeLa66Connection();
                showToast("LA66 disconnected");
            }
        }
    };

    private void registerUsbReceiver() {
        if (usbReceiverRegistered) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        registerReceiver(usbReceiver, filter);
        usbReceiverRegistered = true;
    }

    private boolean connectLa66IfNeeded() {
        try {
            if (MyApplication.port != null && isLa66Connected) {
                return true;
            }

            usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            if (usbManager == null) {
                showToast("USB manager not available");
                return false;
            }

            List<UsbSerialDriver> availableDrivers =
                    UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

            if (availableDrivers == null || availableDrivers.isEmpty()) {
                isLa66Connected = false;
                Log.e("SplashUSB", "No USB serial driver found");
                showToast("LA66 USB device not found");
                return false;
            }

            usbDriver = availableDrivers.get(0);

            if (!usbManager.hasPermission(usbDriver.getDevice())) {
                waitingForUsbPermission = true;

                PendingIntent permissionIntent = PendingIntent.getBroadcast(
                        this,
                        0,
                        new Intent(ACTION_USB_PERMISSION),
                        0
                );

                usbManager.requestPermission(usbDriver.getDevice(), permissionIntent);
                showToast("Please allow USB permission");
                return false;
            }

            return openLa66Connection();

        } catch (Exception e) {
            Log.e("SplashUSB", "connectLa66IfNeeded failed", e);
            showToast("LA66 connection failed");
            return false;
        }
    }

    private boolean openLa66Connection() {
        try {
            if (usbManager == null) {
                usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            }

            if (usbManager == null) {
                showToast("USB manager not available");
                return false;
            }

            if (usbDriver == null) {
                List<UsbSerialDriver> availableDrivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

                if (availableDrivers == null || availableDrivers.isEmpty()) {
                    isLa66Connected = false;
                    showToast("LA66 USB device not found");
                    return false;
                }

                usbDriver = availableDrivers.get(0);
            }

            usbConnection = usbManager.openDevice(usbDriver.getDevice());

            if (usbConnection == null) {
                isLa66Connected = false;
                showToast("Cannot open USB device");
                Log.e("SplashUSB", "usbConnection is null");
                return false;
            }

            if (MyApplication.port != null) {
                try {
                    MyApplication.port.close();
                } catch (Exception ignored) {
                }
            }

            MyApplication.port = usbDriver.getPorts().get(0);
            MyApplication.port.open(usbConnection);
            MyApplication.port.setParameters(
                    9600,
                    8,
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
            );

            isLa66Connected = true;

            Log.e("SplashUSB", "LA66 connected successfully");
            showToast("LA66 connected");

            return true;

        } catch (Exception e) {
            isLa66Connected = false;
            Log.e("SplashUSB", "openLa66Connection failed", e);
            showToast("Open LA66 failed");
            return false;
        }
    }

    private void closeLa66Connection() {
        isLa66Connected = false;

        try {
            if (MyApplication.port != null) {
                MyApplication.port.close();
            }
        } catch (Exception e) {
            Log.e("SplashUSB", "port close failed", e);
        }

        MyApplication.port = null;

        try {
            if (usbConnection != null) {
                usbConnection.close();
            }
        } catch (Exception e) {
            Log.e("SplashUSB", "connection close failed", e);
        }

        usbConnection = null;
        usbDriver = null;
    }

    private void sendGeneratedAtCommand(String command) {
        try {
            if (!connectLa66IfNeeded()) {
                if (waitingForUsbPermission) {
                    pendingCommandToSend = command;
                }
                return;
            }

            if (MyApplication.port == null) {
                showToast("LA66 port is not ready");
                return;
            }

            String hex1 = TextUtils.strToASCII(command) + "0D0A";
            byte[] data = HexUtil.hexStringToBytes(hex1);

            Log.e("SplashSend", "command = " + command);
            Log.e("SplashSend", "hex1 = " + hex1);
            Log.e("SplashSend", "bytes length = " + data.length);

            MyApplication.port.write(data, 3000);

            Log.e("SplashSend", "write ok");
            showToast("Sent");

        } catch (Exception e) {
            Log.e("SplashSend", "sendGeneratedAtCommand failed", e);
            showToast("Send failed");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        registerUsbReceiver();
        connectLa66IfNeeded();

        // Customize status bar and navigation bar colors for versions >= Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.darker_gray));
            getWindow().setNavigationBarColor(getResources().getColor(android.R.color.darker_gray));
        }

        // Customize system UI for versions >= Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        final EditText inputShellMessage = findViewById(R.id.input_shell_message);
        /*final EditText inputShellId = findViewById(R.id.input_shell_id);*/
        final EditText inputDestinationId = findViewById(R.id.input_destination_id);
        final EditText inputSourceId = findViewById(R.id.input_source_id);
        final TextView txtShellSentHistory = findViewById(R.id.txt_shell_sent_history);
        final ScrollView panelShellSentHistory = findViewById(R.id.panel_shell_sent_history);

        inputDestinationId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        inputSourceId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        findViewById(R.id.btn_shell_set_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destinationId = inputDestinationId.getText().toString().trim();
                String sourceId = inputSourceId.getText().toString().trim();
                if (!destinationId.matches("\\d{2}") || !sourceId.matches("\\d{2}")) {
                    showToast("Each ID part must be exactly 2 digits");
                    return;
                }
                selectedDestinationId = destinationId;
                selectedSourceId = sourceId;
                selectedShellId = destinationId + sourceId;
                showToast("ID set: " + selectedShellId);
            }
        });

        /*inputShellId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        // Set ID on button click
        findViewById(R.id.btn_shell_set_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idValue = inputShellId.getText().toString().trim();
                if (!idValue.matches("\\d{4}")) {
                    showToast("ID must be exactly 4 digits");
                    return;
                }
                selectedShellId = idValue;  // Store ID entered by user
                showToast("ID set: " + selectedShellId);
            }
        });*/

        // Send message on button click
        findViewById(R.id.btn_shell_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = inputShellMessage.getText().toString();

                // Check if the input is empty
                if (input.length() == 0) {
                    showToast(getString(R.string.shell_error_empty));
                    return;
                }

                // Check if ID is set
                if (selectedShellId.length() != 4) {
                    showToast("Please set a 4-digit ID first");
                    return;
                }

                // Check if the input contains only printable ASCII characters
                if (!isPrintableAscii(input)) {
                    showToast(getString(R.string.shell_error_ascii));
                    return;
                }

                // Separate the first 4 digits (ID) and the remaining message
                String idPart = selectedShellId;  // Use the selected 4-digit ID
                String messagePart = input; // Message

                // Convert the remaining message to hex
                byte[] payloadBytes = messagePart.getBytes(StandardCharsets.US_ASCII);
                String hexPayload = bytesToHex(payloadBytes);

                // Calculate the total length of the message (ID + message)
                int payloadLength = 2 + payloadBytes.length; // 2 bytes for ID + message length in hex

                if (payloadLength > MAX_PAYLOAD_BYTES) {
                    showToast("Payload too long. Max is " + MAX_PAYLOAD_BYTES + " bytes");
                    return;
                }

                // Construct the final command
                /* String command = SEND_PREFIX + payloadLength + "," + idPart + hexPayload;*/
                String command = SEND_PREFIX + payloadLength + "," + selectedDestinationId + selectedSourceId + hexPayload;

                // Display the command in the history view
                appendOutput(txtShellSentHistory, command, panelShellSentHistory);
                appendOutput(txtShellSentHistory, "send to " + selectedDestinationId + ": " + messagePart, panelShellSentHistory);

                // Send the real AT command to LA66
                sendGeneratedAtCommand(command);

                // Clear the input field
                inputShellMessage.setText("");
            }
        });

        // Open legacy UI
        findViewById(R.id.btn_open_legacy_ui).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (usbReceiverRegistered) {
            try {
                unregisterReceiver(usbReceiver);
            } catch (Exception ignored) {
            }
            usbReceiverRegistered = false;
        }

        closeLa66Connection();
    }

    // Function to append output to the screen
    private void appendOutput(TextView outputView, String line, ScrollView container) {
        CharSequence current = outputView.getText();
        if (current == null || current.length() == 0) {
            outputView.setText(line);
        } else {
            outputView.append("\n" + line);
        }
        container.post(new Runnable() {
            @Override
            public void run() {
                container.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    // Function to check if input contains only printable ASCII characters
    private boolean isPrintableAscii(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < 0x20 || c > 0x7E) {
                return false;
            }
        }
        return true;
    }

    // Function to convert bytes to hex string
    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte data : bytes) {
            builder.append(String.format("%02X", data));
        }
        return builder.toString();
    }

    // Function to show toast messages
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}