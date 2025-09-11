package com.example.iotsmssender;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "IoTSmsSender";
    private static final String DEVICE_ADDRESS = "00:22:06:01:11:77"; // HC-06 MAC
    private static final int SMS_PERMISSION_CODE = 1;
    private static final int BLUETOOTH_PERMISSION_CODE = 2;
    private static final int MAX_SMS_PER_ALERT = 5; // max messages per alert type

    private BluetoothSocket socket;
    private ProgressBar progressBar;
    private InputStream inputStream;
    private EditText targetNumberInput;
    private Button startButton;
    private String targetNumber = "";


    private final Map<String, Integer> alertCount = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button moreOptionsButton = findViewById(R.id.moreOptionsButton);
        TextView teamLink = findViewById(R.id.teamMembersLink);
        moreOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });
        // UI elements
        targetNumberInput = findViewById(R.id.targetNumberInput);
        startButton = findViewById(R.id.startButton);
        ImageView gifImage = findViewById(R.id.gifImage);
        progressBar = findViewById(R.id.progressBar);
        teamLink.setPaintFlags(teamLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        teamLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TeamActivity.class));
            }
        });


        // Load GIF
        Glide.with(this).asGif().load(R.raw.fire).into(gifImage);


        // Request SMS & Bluetooth permissions
        requestAllPermissions();

        // Start button click
        startButton.setOnClickListener(v -> {
            String userInput = targetNumberInput.getText().toString().trim();
            if (!userInput.isEmpty() && userInput.startsWith("0")) {
                targetNumber = "+88" + userInput;
                Log.d(TAG, "Target number saved: " + targetNumber);
                Toast.makeText(this, "Monitoring started!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.VISIBLE);
                if (hasBluetoothPermission()) {
                    connectBluetooth();
                } else {
                    requestBluetoothPermission();
                }
            } else {
                Log.e(TAG, "Enter a valid number starting with 0");
                Toast.makeText(this, "Enter a valid number starting with 0", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Permissions
    private void requestAllPermissions() {
        if (!hasSmsPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
        if (!hasBluetoothPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                    BLUETOOTH_PERMISSION_CODE);
        }
    }

    private boolean hasSmsPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasBluetoothPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                BLUETOOTH_PERMISSION_CODE);
    }

    // Connect Bluetooth
    @SuppressLint("MissingPermission")
    private void connectBluetooth() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            startButton.setText("Connecting..."); // show connecting state
        });

        new Thread(() -> {
            try {
                if (!hasBluetoothPermission()) {
                    Log.e(TAG, "Bluetooth connect permission not granted!");
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        startButton.setText("Start Monitoring"); // reset if failed
                    });
                    return;
                }

                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) {
                    Log.e(TAG, "No Bluetooth adapter found");
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        startButton.setText("Start Monitoring"); // reset if failed
                    });
                    return;
                }

                BluetoothDevice device = adapter.getRemoteDevice(DEVICE_ADDRESS);
                socket = device.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                );
                adapter.cancelDiscovery();
                socket.connect();
                inputStream = socket.getInputStream();
                Log.d(TAG, "Bluetooth connected");


                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    startButton.setText("Connected ✅");
                    Toast.makeText(MainActivity.this, "Bluetooth Connected!", Toast.LENGTH_SHORT).show();
                });

                listenForMessages();

            } catch (SecurityException se) {
                Log.e(TAG, "Bluetooth connect failed - permission denied", se);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    startButton.setText("Start Monitoring"); // reset
                });
            } catch (Exception e) {
                Log.e(TAG, "Bluetooth connection error", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    startButton.setText("Start Monitoring"); // reset
                });
            }
        }).start();
    }


    // Listen for incoming Bluetooth messages
    private void listenForMessages() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            try {
                while (true) {
                    if ((bytes = inputStream.read(buffer)) > 0) {
                        String message = new String(buffer, 0, bytes).trim();
                        Log.d(TAG, "Received message: " + message);

                        if (targetNumber.isEmpty()) {
                            Log.e(TAG, "Target number not set. Cannot send SMS");
                            continue;
                        }

                        // Determine alert type
                        String alertType = null;
                        String alertText = null;

                        if (message.contains("DANGER")) {
                            alertType = "DANGER";
                            alertText = "🚨 DANGER: Smoke level very HIGH!";
                        } else if (message.contains("MORE DANGER")) {
                            alertType = "MORE DANGER";
                            alertText = "⚠️ MORE DANGER: Smoke level rising!";
                        } else if (message.contains("WARNING")) {
                            alertType = "WARNING";
                            alertText = "⚠️ WARNING: Smoke detected!";
                        }

                        if (alertType != null) {
                            int count = alertCount.getOrDefault(alertType, 0);
                            if (count < MAX_SMS_PER_ALERT) {
                                sendSMS(alertText);
                                alertCount.put(alertType, count + 1);
                            } else {
                                Log.d(TAG, alertType + " SMS limit reached, skipping.");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading from Bluetooth: ", e);
            }
        }).start();
    }

    // Send SMS
    private void sendSMS(String text) {
        if (!hasSmsPermission()) {
            Log.e(TAG, "SMS permission not granted!");
            requestAllPermissions();
            return;
        }

        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(targetNumber, null, text, null, null);
            Log.d(TAG, "SMS sent to " + targetNumber + ": " + text);
        } catch (SecurityException se) {
            Log.e(TAG, "SMS failed - permission denied", se);
        } catch (Exception e) {
            Log.e(TAG, "SMS failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "SMS permission granted");
            } else {
                Log.e(TAG, "SMS permission denied");
            }
        } else if (requestCode == BLUETOOTH_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Bluetooth permission granted");
                connectBluetooth();
            } else {
                Log.e(TAG, "Bluetooth permission denied");
            }
        }
    }
}
