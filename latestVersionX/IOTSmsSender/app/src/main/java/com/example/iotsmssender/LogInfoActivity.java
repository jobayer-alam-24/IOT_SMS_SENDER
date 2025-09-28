package com.example.iotsmssender;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LogInfoActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private Button backBtn;
    private RecyclerView recyclerView;
    private TextView tvNoLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);
        recyclerView = findViewById(R.id.recyclerViewLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvNoLogs = findViewById(R.id.tvNoLogs);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInfoActivity.this, MainActivity.class));
            }
        });
        List<Map<String, String>> logs = dbHelper.fetchDetections();

        if (logs.isEmpty()) {
            tvNoLogs.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoLogs.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_log, parent, false);
                    return new RecyclerView.ViewHolder(view) {};
                }

                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                    Map<String, String> log = logs.get(position);
                    View itemView = holder.itemView;

                    TextView tvType = itemView.findViewById(R.id.tvDetectionType);
                    TextView tvDate = itemView.findViewById(R.id.tvDate);
                    TextView tvTime = itemView.findViewById(R.id.tvTime);

                    String type = log.get("type");
                    String date = log.get("date");
                    String timeFromDB = log.get("time");

                    try {
                        SimpleDateFormat sdfUtc = new SimpleDateFormat("HH:mm");
                        sdfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date parsedTime = sdfUtc.parse(timeFromDB);

                        SimpleDateFormat sdfBD = new SimpleDateFormat("hh:mm a");
                        sdfBD.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
                        String bdTime = sdfBD.format(parsedTime);

                        tvTime.setText(bdTime);
                    } catch (Exception e) {
                        tvTime.setText(timeFromDB);
                    }
                    tvType.setText(type + " DETECTED");
                    tvDate.setText(date);

                    switch (type) {
                        case "DANGER":
                            tvType.setTextColor(0xFFFF0000);
                            break;
                        case "MORE DANGER":
                            tvType.setTextColor(0xFFFFA500);
                            break;
                        case "WARNING":
                            tvType.setTextColor(0xFFFFFF00);
                            break;
                        default:
                            tvType.setTextColor(0xFF333333);
                    }
                }

                @Override
                public int getItemCount() {
                    return logs.size();
                }
            });
        }
    }
}
