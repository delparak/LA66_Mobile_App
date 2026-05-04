package com.sz.cp2102;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sz.cp2102.MyApplication;
import com.sz.cp2102.MainActivity;

import android.util.Log;

import com.clj.fastble.utils.HexUtil;
import com.sz.cp2102.utils.TextUtils;

public class SplashActivity extends Activity {

    private static final int MAX_PAYLOAD_BYTES = 16;
    private static final String SEND_PREFIX = "AT+SENDB=01,02,";
    private String selectedShellId = "";

    private void sendGeneratedAtCommand(String command) {
        try {
            if (MyApplication.port == null) {
                showToast("LA66 is not connected");
                return;
            }

            String hex1 = TextUtils.strToASCII(command) + "0D0A";
            Log.e("SplashSend", "command = " + command);
            Log.e("SplashSend", "hex1 = " + hex1);

            MyApplication.port.write(HexUtil.hexStringToBytes(hex1), 3000);

        } catch (Exception e) {
            Log.e("SplashSend", "sendGeneratedAtCommand failed", e);
            showToast("Send failed");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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
                selectedShellId = destinationId + sourceId;
                showToast("ID set: " + selectedShellId);
            }
        });

        findViewById(R.id.btn_shell_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = inputShellMessage.getText().toString();

                // Check if the input is empty
                if (input.length() == 0) {
                    showToast(getString(R.string.shell_error_empty));
                    return;
                }

                if (selectedShellId.length() != 4) {
                    showToast("Please set a 4-digit ID first");
                    return;
                }

                // Check if the input contains only printable ASCII characters
                if (!isPrintableAscii(input)) {
                    showToast(getString(R.string.shell_error_ascii));
                    return;
                }

                byte[] payloadBytes = input.getBytes(StandardCharsets.US_ASCII);

                // Check if the input exceeds the max allowed bytes
                if (payloadBytes.length > MAX_PAYLOAD_BYTES) {
                    showToast(getString(R.string.shell_error_max_bytes, MAX_PAYLOAD_BYTES));
                    return;
                }

                String hexPayload = bytesToHex(payloadBytes);
                String command = SEND_PREFIX + payloadBytes.length + "," + hexPayload;

                // Display the command in the history view
                appendOutput(txtShellSentHistory, "TX: " + command, panelShellSentHistory);

                // Send real AT command to LA66
                sendGeneratedAtCommand(command);

                inputShellMessage.setText("");
            }
        });

        findViewById(R.id.btn_open_legacy_ui).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
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
