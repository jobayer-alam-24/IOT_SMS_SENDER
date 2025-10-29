package com.example.iotsmssender;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
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
    private boolean isBengali = false; // default language English

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecycler = findViewById(R.id.chatRecycler);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));

        chatAdapter = new ChatAdapter(messageList);
        chatRecycler.setAdapter(chatAdapter);

        backBtn = findViewById(R.id.backBtn);
        Switch langToggle = findViewById(R.id.langToggle);

        // Text-to-Speech setup
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });

        // Toggle for English ↔ Bengali
        langToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isBengali = isChecked;
            if (isBengali) {
                tts.setLanguage(new Locale("bn", "BD"));
            } else {
                tts.setLanguage(Locale.US);
            }

            messageList.clear();
            loadMessages(); // reload based on selected language
            chatAdapter.notifyDataSetChanged();
        });

        backBtn.setOnClickListener(v -> startActivity(new Intent(ChatActivity.this, MainActivity.class)));

        // Load default messages (English first)
        loadMessages();
    }


    private void loadMessages() {
        if (!isBengali) {
            // English Q&A
            addMessage("Who developed this project?", true);
            addMessage("This IoT SMS Sender project was developed by Sheikh Jobayer Alam and his team members Sabbir Hossen and Touhid Billah, under the guidance of our honourable teachers MD. Shimul Hussain and Tanvir Mahtab.", false);

            addMessage("What is this app for?", true);
            addMessage("This app connects to an IoT device with a gas sensor and Bluetooth. It sends SMS alerts to a target number if dangerous gas is detected.", false);

            addMessage("Why was this app made?", true);
            addMessage("The app was designed to quickly detect dangerous gases and alert a responsible person immediately to prevent accidents.", false);

            addMessage("What hardware did you use for this project?", true);
            addMessage("We used Arduino Uno R3, HC-6 Bluetooth module, MQ-2 gas sensor, buzzer, LED, jumper wires, resistors, and a breadboard.", false);

            addMessage("Which programming language did you use for the app?", true);
            addMessage("The Android app is developed using Java in Android Studio.", false);

            addMessage("How does the Arduino communicate with the app?", true);
            addMessage("The Arduino sends sensor readings to the app via the HC-6 Bluetooth module.", false);
        } else {
            // Bengali Q&A
            addMessage("এই প্রজেক্টটি কে তৈরি করেছে?", true);
            addMessage("এই IoT SMS Sender প্রজেক্টটি তৈরি করেছে শেখ জোবায়ের আলম এবং তার টিম সদস্যরা সাব্বির হোসেন ও তৌহিদ বিল্লাহ, আমাদের সম্মানিত শিক্ষক মোঃ শিমুল হোসেন এবং তানভির মাহতাব এর তত্ত্বাবধানে।", false);

            addMessage("এই অ্যাপটি কী কাজ করে?", true);
            addMessage("এই অ্যাপটি একটি IoT ডিভাইসের সাথে যুক্ত হয় যা গ্যাস সেন্সরের মাধ্যমে বিপজ্জনক গ্যাস শনাক্ত করে এবং নির্দিষ্ট নম্বরে এসএমএস পাঠায়।", false);

            addMessage("এই অ্যাপটি কেন তৈরি করা হয়েছে?", true);
            addMessage("বিপজ্জনক গ্যাস দ্রুত শনাক্ত করে দায়িত্বশীল ব্যক্তিকে সতর্ক বার্তা পাঠানোর জন্য এই অ্যাপটি তৈরি করা হয়েছে।", false);

            addMessage("এই প্রজেক্টে কোন হার্ডওয়্যার ব্যবহার করা হয়েছে?", true);
            addMessage("আমরা ব্যবহার করেছি Arduino Uno R3, HC-6 Bluetooth module, MQ-2 gas sensor, buzzer, LED, jumper wires, resistors এবং breadboard।", false);

            addMessage("অ্যাপটি কোন ভাষায় তৈরি?", true);
            addMessage("অ্যাপটি Android Studio-তে Java ভাষায় তৈরি করা হয়েছে।", false);

            addMessage("Arduino কীভাবে অ্যাপের সাথে যোগাযোগ করে?", true);
            addMessage("Arduino HC-6 Bluetooth module এর মাধ্যমে সেন্সরের ডেটা অ্যাপে পাঠায়।", false);
        }
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
                        if (isBengali) {
                            tts.setLanguage(new Locale("bn", "BD"));
                        } else {
                            tts.setLanguage(Locale.US);
                        }
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
