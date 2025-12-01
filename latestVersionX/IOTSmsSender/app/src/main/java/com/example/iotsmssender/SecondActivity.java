package com.example.iotsmssender;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SecondActivity extends AppCompatActivity {
    private Button backBtn;
    private ImageButton hospitalCard, fireStationCard, ambulanceCard, waterCard;
    private ImageButton hospitalCall, fireStationCall, ambulanceCall;
    private ImageView fireIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.second), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Cards
        hospitalCard = findViewById(R.id.gpsButton1);
        fireStationCard = findViewById(R.id.gpsButton2);
        ambulanceCard = findViewById(R.id.gpsButton3);
        backBtn = findViewById(R.id.backBtn);
        fireIcon = findViewById(R.id.fireIcon);
        waterCard = findViewById(R.id.gpsButton4);


        // Initialize Call Buttons
        hospitalCall = findViewById(R.id.hospitalCall);
        fireStationCall = findViewById(R.id.fireStationCall);
        ambulanceCall = findViewById(R.id.ambulanceCall);
        hospitalCall.setOnClickListener(v -> openDialer("999"));
        fireStationCall.setOnClickListener(v -> openDialer("999"));
        ambulanceCall.setOnClickListener(v -> openDialer("999"));
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SecondActivity.this, MainActivity.class));
            }
        });
        fireIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SecondActivity.this, InsertActivity.class));
            }
        });
        // Hospital
        hospitalCard.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:23.8103,90.4125?q=hospital+near+me"));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });
        //water
        // Water Source
        waterCard.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:23.8130,90.4140?q=(water+source)|(water+hydrant)|(pond)|(lake)|(firefighting+water)+near+me"));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        // Fire Station
        fireStationCard.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:23.8110,90.4120?q=fire+station+near+me"));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        // Ambulance
        ambulanceCard.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:23.8120,90.4130?q=ambulance+near+me"));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

    }
    private void openDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
}