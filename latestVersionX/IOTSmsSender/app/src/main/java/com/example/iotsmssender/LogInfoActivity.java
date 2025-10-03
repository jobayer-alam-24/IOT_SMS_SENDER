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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

            recyclerView.setAdapter(new RecyclerView.Adapter<LogViewHolder>() {
                @Override
                public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_log, parent, false);
                    return new LogViewHolder(view);
                }

                @Override
                public void onBindViewHolder(LogViewHolder holder, int position) {
                    Map<String, String> log = logs.get(position);

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

                        holder.tvTime.setText(bdTime);
                    } catch (Exception e) {
                        holder.tvTime.setText(timeFromDB);
                    }

                    holder.tvType.setText(type + " DETECTED");
                    holder.tvDate.setText(date);

                    switch (type) {
                        case "DANGER":
                            holder.tvType.setTextColor(0xFFFF0000);
                            break;
                        case "MORE DANGER":
                            holder.tvType.setTextColor(0xFFFFA500);
                            break;
                        case "WARNING":
                            holder.tvType.setTextColor(0xFFFFFF00);
                            break;
                        default:
                            holder.tvType.setTextColor(0xFF333333);
                    }
                }

                @Override
                public int getItemCount() {
                    return logs.size();
                }
            });
        }
    }

    // ViewHolder class
    private static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvTime;

        LogViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvDetectionType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
