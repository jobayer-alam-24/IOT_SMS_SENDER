package com.example.iotsmssender;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.GetCredentialException;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.SignInButton;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "IoTSmsSender";
    private static final String DEVICE_ADDRESS = "00:22:06:01:11:77"; // HC-06 MAC
    private static final int SMS_PERMISSION_CODE = 1;
    private static final int BLUETOOTH_PERMISSION_CODE = 2;
    private static final int PICK_CONTACT = 102;
    private static final int REQ_CONTACT_PERMISSION = 103;
    private static final int MAX_SMS_PER_ALERT = 5; // max messages per alert type

    private BluetoothSocket socket;
    private boolean robotShown = false;
    private ProgressBar progressBar;
    private InputStream inputStream;
    private EditText targetNumberInput;
    private Button startButton;
    private ImageView pickContactButton;
    private TextView robotButton;
    private String targetNumber = "";

    private CredentialManager credentialManager;
    private GetCredentialRequest request;
    SignInButton googleSignInButton;
    DBHelper dbHelper;


    private final Map<String, Integer> alertCount = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        credentialManager = CredentialManager.create(this);
        GetSignInWithGoogleOption googleOption = new GetSignInWithGoogleOption.Builder(getString(R.string.server_client_id))
                .setNonce(java.util.UUID.randomUUID().toString())
                .build();
        request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build();



        dbHelper = new DBHelper(this);
        robotButton = findViewById(R.id.robotButton);
        robotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open ChatActivity
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });
        new Handler().postDelayed(() -> {
            if (!robotShown) {
                robotButton.setVisibility(View.VISIBLE);
                robotButton.setTranslationX(-500f);
                robotButton.animate()
                        .translationX(0f)
                        .setDuration(800)
                        .start();
                robotShown = true; // mark as shown
            }
        }, 3000);
        Button moreOptionsButton = findViewById(R.id.moreOptionsButton);
        TextView teamLink = findViewById(R.id.teamMembersLink);
        moreOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGoogleLogin();
            }
        });
        // UI elements
        targetNumberInput = findViewById(R.id.targetNumberInput);
        startButton = findViewById(R.id.startButton);
        pickContactButton = findViewById(R.id.pickContactButton);
        progressBar = findViewById(R.id.progressBar);
        teamLink.setPaintFlags(teamLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        teamLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TeamActivity.class));
            }
        });

        Button logInfoButton = findViewById(R.id.logInfoButton);
        logInfoButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LogInfoActivity.class));
        });



        // Request SMS & Bluetooth permissions
        requestAllPermissions();
        pickContactButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQ_CONTACT_PERMISSION);
            } else {
                openContactPicker();
            }
        });
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
    private void requestGoogleLogin()
    {
        credentialManager.getCredentialAsync(this, request, null, Runnable::run, new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse getCredentialResponse) {
                Toast.makeText(MainActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {
                Toast.makeText(MainActivity.this, "Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
            }
        });

    };

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
    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String number = cursor.getString(0);
                cursor.close();

                // Clean up number (remove spaces, dashes)
                number = number.replaceAll("\\s+", "").replaceAll("-", "");

                // Set into EditText
                targetNumberInput.setText(number);

                // Format with +88 if needed
                if (number.startsWith("0")) {
                    targetNumber = "+88" + number;
                } else {
                    targetNumber = number;
                }

                Log.d(TAG, "Picked contact number: " + targetNumber);
                Toast.makeText(this, "Contact selected!", Toast.LENGTH_SHORT).show();
            }
        }
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
                            storeDetectionInDB(alertType);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading from Bluetooth: ", e);
            }
        }).start();
    }

    public boolean hasInternetConnection(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
    private void storeDetectionInDB(String alertType) {
        if (alertType == null || alertType.isEmpty()) return;
        dbHelper.insertDetection(alertType);
        Toast.makeText(this, "Detection stored locally", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Detection stored: " + alertType);
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("detections");

            String id = ref.push().getKey();
            Detection detection = new Detection(alertType, System.currentTimeMillis());

            ref.child(id).setValue(detection)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Detection sent to Firebase"))
                    .addOnFailureListener(e -> Log.e(TAG, "Firebase Error", e));
        } catch (Exception e) {
            Log.e(TAG, "Firebase store failed", e);
        }
        new Thread(() -> {
            try {
                URL url = new URL("https://jobayer.wuaze.com/insert_detection.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String postData = "Type=" + alertType;

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("PHP_RESPONSE", "Response Code: " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                Log.d("PHP_RESPONSE", "Response: " + response.toString());
            } catch (Exception e) {
                Log.e("PHP_RESPONSE", "Error sending detection: ", e);
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
        }else if (requestCode == REQ_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactPicker();
            } else {
                Toast.makeText(this, "Contacts permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
   }