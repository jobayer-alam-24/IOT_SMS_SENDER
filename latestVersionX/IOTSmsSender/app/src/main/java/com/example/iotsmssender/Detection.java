package com.example.iotsmssender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Detection {
    public String type;
    public long timestamp;
    public String time;
    public String day;
    public String date;

    public Detection() { }

    public Detection(String type, long timestamp) {
        this.type = type;
        this.timestamp = timestamp;


        Date dateObj = new Date(timestamp);

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        this.time = timeFormat.format(dateObj);
        this.day = dayFormat.format(dateObj);
        this.date = dateFormat.format(dateObj);
    }
}
