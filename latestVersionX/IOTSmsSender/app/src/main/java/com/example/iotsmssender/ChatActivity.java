package com.example.iotsmssender;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecycler;
    private ChatAdapter chatAdapter;
    private List<Message> messageList = new ArrayList<>();
    private TextToSpeech tts;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecycler = findViewById(R.id.chatRecycler);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));

        chatAdapter = new ChatAdapter(messageList);
        chatRecycler.setAdapter(chatAdapter);
        backBtn = findViewById( R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatActivity.this, MainActivity.class));
            }
        });

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });

        // Project credits
        addMessage("Who developed this project?", true);
        addMessage("This IoT SMS Sender project was developed by Sheikh Jobayer Alam and his team members Sabbir Hossen and Touhid Billah, under the guidance of our honourable teachers MD. Shimul Hussain (Instructor, IT Support & IoT Basics) and Tanvir Mahtab (Junior Instructor, IT Support & IoT Basics).", false);

// App overview
        addMessage("What is this app for?", true);
        addMessage("This app connects to an IoT device with a gas sensor and Bluetooth. It sends SMS alerts to a target number if dangerous gas is detected.", false);

        addMessage("Why was this app made?", true);
        addMessage("The app was designed to quickly detect dangerous gases and alert a responsible person immediately to prevent accidents.", false);

        addMessage("How do I use the app?", true);
        addMessage("Turn on the Arduino device, ensure Bluetooth is on, set a target number manually or from contacts, and tap 'Start Monitoring'. You will receive alerts via SMS if gas is detected.", false);

        addMessage("Will the app send SMS automatically?", true);
        addMessage("Yes, once a dangerous gas level is detected, the app sends an SMS immediately to the target number you set.", false);

        addMessage("Can I see past detections?", true);
        addMessage("Yes, tap 'Detection History' from the main screen to see a log of all alerts with date and time. SQLite is used to store these records.", false);

        addMessage("How do I call emergency services?", true);
        addMessage("Tap 'Emergency Contacts' to see nearby hospitals, fire stations, and ambulances. You can call directly from the app.", false);

        addMessage("Do I need internet for this app?", true);
        addMessage("No, the app uses Bluetooth to communicate with the device and SMS to send alerts, so internet is not required.", false);

        addMessage("Can I test the app without gas?", true);
        addMessage("Yes, you can simulate gas detection by triggering alerts in the Arduino code or manually sending test messages.", false);

        addMessage("Is SMS free?", true);
        addMessage("The app uses your phone's SMS service, so standard SMS charges may apply depending on your mobile plan.", false);

// Hardware components
        addMessage("What hardware did you use for this project?", true);
        addMessage("We used Arduino Uno R3, HC-6 Bluetooth module, MQ-2 gas sensor, buzzer, LED, jumper wires, resistors, and a breadboard.", false);

        addMessage("Which programming language did you use for the app?", true);
        addMessage("The Android app is developed using Java in Android Studio.", false);

        addMessage("How does the Arduino communicate with the app?", true);
        addMessage("The Arduino sends sensor readings to the app via the HC-6 Bluetooth module.", false);

        addMessage("Why did you choose HC-6 Bluetooth module?", true);
        addMessage("HC-6 is easy to interface with Arduino and supports serial communication with Android apps.", false);

        addMessage("What sensor is used for gas detection?", true);
        addMessage("We used the MQ-2 sensor, which detects flammable gases like LPG, methane, and smoke.", false);

        addMessage("What is the purpose of the buzzer and LED?", true);
        addMessage("The buzzer and LED provide immediate visual and audible alerts when dangerous gas is detected.", false);

        addMessage("Do I need any extra hardware to run the app?", true);
        addMessage("You only need the Arduino setup with the components listed. The app runs on Android and communicates via Bluetooth.", false);

        addMessage("Can I use another Arduino board?", true);
        addMessage("Yes, other Arduino boards with enough digital pins and Bluetooth support can be used with minor code adjustments.", false);

        addMessage("What resistors are used and why?", true);
        addMessage("We used standard 220Ω and 10kΩ resistors for the LED and sensor voltage divider to ensure proper current and voltage levels.", false);

        addMessage("Do I need to assemble the hardware manually?", true);
        addMessage("Yes, you need to connect the Arduino, sensor, LED, buzzer, and Bluetooth module on a breadboard using jumper wires.", false);

        addMessage("Which IDE is used for Arduino programming?", true);
        addMessage("We used Arduino IDE to write and upload the code to Arduino Uno R3.", false);

        addMessage("How is the sensor calibrated?", true);
        addMessage("The MQ-2 sensor should be powered for a few minutes before readings stabilize. You can adjust the threshold in the Arduino code.", false);

        addMessage("Can I expand this project?", true);
        addMessage("Yes, you can add multiple sensors, integrate Wi-Fi modules, or use advanced alert systems for a more robust IoT solution.", false);

// App connection & functionality
        addMessage("How do I connect my device to the app?", true);
        addMessage("Turn on Bluetooth on your phone and ensure your Arduino is powered on. Then, tap 'Start Monitoring' to connect.", false);

        addMessage("How do I set the target number for SMS?", true);
        addMessage("Enter the number in the target number field or pick from your contacts, then tap 'Start Monitoring'.", false);

        addMessage("What happens if I forget to set a target number?", true);
        addMessage("The app will prompt you to enter or select a target number before it can send an SMS.", false);

        addMessage("How do I know if my device is connected?", true);
        addMessage("When connected, the 'Start Monitoring' button will show that monitoring is active. You can also see the real-time status in the app.", false);

        addMessage("Can I use multiple target numbers?", true);
        addMessage("Currently, the app allows only one target number at a time. You can change it anytime by entering a new number or selecting another contact.", false);

    }

    private void addMessage(String text, boolean isQuestion) {
        messageList.add(new Message(text, isQuestion));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecycler.scrollToPosition(messageList.size() - 1);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    static class Message {
        String text;
        boolean isQuestion;

        Message(String text, boolean isQuestion) {
            this.text = text;
            this.isQuestion = isQuestion;
        }
    }

    class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_QUESTION = 1;
        private static final int TYPE_ANSWER = 2;
        private final List<Message> messages;

        ChatAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isQuestion ? TYPE_QUESTION : TYPE_ANSWER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_QUESTION) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
                return new QuestionViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_answer, parent, false);
                return new AnswerViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Message message = messages.get(position);

            if (holder instanceof QuestionViewHolder) {
                ((QuestionViewHolder) holder).questionText.setText(message.text);
            } else if (holder instanceof AnswerViewHolder) {
                ((AnswerViewHolder) holder).answerText.setText(message.text);
                ((AnswerViewHolder) holder).speaker.setOnClickListener(v -> {
                    if (tts != null) {
                        tts.speak(message.text, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class QuestionViewHolder extends RecyclerView.ViewHolder {
            TextView questionText;
            QuestionViewHolder(View itemView) {
                super(itemView);
                questionText = itemView.findViewById(R.id.questionText);
            }
        }

        class AnswerViewHolder extends RecyclerView.ViewHolder {
            TextView answerText;
            ImageView speaker;
            AnswerViewHolder(View itemView) {
                super(itemView);
                answerText = itemView.findViewById(R.id.answerText);
                speaker = itemView.findViewById(R.id.speakerIcon);
            }
        }
    }
}
