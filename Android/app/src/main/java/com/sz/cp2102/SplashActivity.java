package com.sz.cp2102;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;

public class SplashActivity extends Activity {

    private static final int MAX_PAYLOAD_BYTES = 16;
    private static final String SEND_PREFIX = "AT+SENDB=01,02,";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.darker_gray));
            getWindow().setNavigationBarColor(getResources().getColor(android.R.color.darker_gray));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        final EditText inputShellMessage = findViewById(R.id.input_shell_message);
        final TextView txtShellSentHistory = findViewById(R.id.txt_shell_sent_history);
        final ScrollView panelShellSentHistory = findViewById(R.id.panel_shell_sent_history);

        findViewById(R.id.btn_shell_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = inputShellMessage.getText().toString();
                if (input.length() == 0) {
                    showToast(getString(R.string.shell_error_empty));
                    return;
                }
                if (!isPrintableAscii(input)) {
                    showToast(getString(R.string.shell_error_ascii));
                    return;
                }

                byte[] payloadBytes = input.getBytes(StandardCharsets.US_ASCII);
                if (payloadBytes.length > MAX_PAYLOAD_BYTES) {
                    showToast(getString(R.string.shell_error_max_bytes, MAX_PAYLOAD_BYTES));
                    return;
                }

                String hexPayload = bytesToHex(payloadBytes);
                String command = SEND_PREFIX + payloadBytes.length + "," + hexPayload;
<<<<<<< codex/review-repository-functionality-2q5m8m
                if (sendCommand(command)) {
                    appendOutput(txtShellSentHistory, "TX: " + command, panelShellSentHistory);
                    inputShellMessage.setText("");
                } else {
                    showToast(getString(R.string.shell_error_not_connected));
                }
=======
                appendOutput(txtShellSentHistory, "TX: " + command, panelShellSentHistory);
                inputShellMessage.setText("");
>>>>>>> main
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

    private boolean isPrintableAscii(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < 0x20 || c > 0x7E) {
                return false;
            }
        }
        return true;
    }
<<<<<<< codex/review-repository-functionality-2q5m8m

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte data : bytes) {
            builder.append(String.format("%02X", data));
        }
        return builder.toString();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean sendCommand(String command) {
        if (MyApplication.port == null) {
            return false;
        }
        try {
            String hexCommand = TextUtils.strToASCII(command) + "0D0A";
            MyApplication.port.write(HexUtil.hexStringToBytes(hexCommand), 3000);
            return true;
        } catch (Exception e) {
            return false;
        }
=======

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte data : bytes) {
            builder.append(String.format("%02X", data));
        }
        return builder.toString();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
>>>>>>> main
    }
}