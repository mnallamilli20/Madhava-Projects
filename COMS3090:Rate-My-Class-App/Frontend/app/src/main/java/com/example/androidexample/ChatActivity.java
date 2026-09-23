package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//added
import android.content.SharedPreferences;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;
import android.net.Uri;

import org.java_websocket.handshake.ServerHandshake;

/**
 * ChatActivity handles the chat interface where users can send and receive messages
 * using a WebSocket connection.
 */
public class ChatActivity extends AppCompatActivity implements ChatWebSocketListener {

    private static final String BASE_URL = "ws://coms-3090-018.class.las.iastate.edu:8080";

    private Button sendBtn;
    private EditText msgEtx;
    private TextView msgTv;
    //added
    private TextView roomInfoTv;

    private ImageButton btnBack;
    private TextView tvChatCourseName, tvConnectionStatus;
    private ScrollView chatScrollView;

    private long courseId = -1L;
    private String username = "Guest";
    private String courseName = "Course Chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /* initialize UI elements */
        sendBtn = (Button) findViewById(R.id.sendBtn);
        msgEtx = (EditText) findViewById(R.id.msgEdt);
        msgTv = (TextView) findViewById(R.id.tx1);
        //added
        roomInfoTv = findViewById(R.id.roomInfoTv);

        btnBack = findViewById(R.id.btnBackChat);
        tvChatCourseName = findViewById(R.id.tvChatCourseName);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        chatScrollView = findViewById(R.id.chatScrollView);

        //gets course info from intent
        courseId = getIntent().getLongExtra("COURSE_ID", -1L);
        courseName = getIntent().getStringExtra("COURSE_NAME");

        //gets username from prefs
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        username = prefs.getString("USERNAME", null);

        //if failed to get username
        if(username == null || username.trim().isEmpty()) {
            username = getIntent().getStringExtra("username");
        }
        if(username == null || username.trim().isEmpty()) {
            username = "Guest";
        }

        if(courseName == null || courseName.trim().isEmpty()) {
            courseName = "Course Chat";
        }

        //sets header
        tvChatCourseName.setText(courseName + " Chat");
        roomInfoTv.setText("Logged in as: " + username);
        tvConnectionStatus.setText("Connecting...");

        //connect to websocket
        if(courseId == -1L) {
            Toast.makeText(this, "Missing course ID", Toast.LENGTH_SHORT).show();
            tvConnectionStatus.setText("Error: No course ID");
            return;
        }

        //url
        String safeUsername = Uri.encode(username);
        String wsUrl = BASE_URL + "/chat/" + courseId + "/" + safeUsername;
        Log.d("ChatActivity", "Connecting to: " + wsUrl);

        /* connect this activity to the websocket instance */
        ChatWebSocketManager.getInstance().setWebSocketListener(ChatActivity.this);
        ChatWebSocketManager.getInstance().connectWebSocket(wsUrl);

        //back button
        btnBack.setOnClickListener(v -> {
            ChatWebSocketManager.getInstance().disconnectWebSocket();
            finish();
        });

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            String msg = msgEtx.getText().toString().trim();

            if(msg.isEmpty()) {
                return;
            }

            try {
                // send message
                //WebSocketManager.getInstance().sendMessage(msgEtx.getText().toString());
                ChatWebSocketManager.getInstance().sendMessage(msg);
                //clear text box once the msg is sent
                msgEtx.setText("");
            } catch (Exception e) {
                //Log.d("ExceptionSendMessage:", e.getMessage().toString());
                Log.e("ChatActivity", "Send failed", e);
                appendLine("ERROR: " + e.getMessage());
            }
        });
    }

    /*
    helper method to append line
     */
    private void appendLine(String text) {
        runOnUiThread(() -> {
                String current = msgTv.getText().toString();
                if(current.isEmpty()) {
                    msgTv.setText(text);
                }
                else {
                    msgTv.setText(current + "\n" + text);
                }
                //auto scroll to bottom
                chatScrollView.post(() ->
                        chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }


    /**
     * Called when a message is received from the WebSocket.
     * This method ensures that UI updates happen on the main thread.
     */
    @Override
    public void onWebSocketMessage(String message) {
        /**
         * In Android, all UI-related operations must be performed on the main UI thread
         * to ensure smooth and responsive user interfaces. The 'runOnUiThread' method
         * is used to post a runnable to the UI thread's message queue, allowing UI updates
         * to occur safely from a background or non-UI thread.
         */
        //runOnUiThread(() -> {
//            String s = msgTv.getText().toString();
//            msgTv.setText(s + "\n"+message);

        appendLine(message);
    }

    /**
     * Called when the WebSocket connection is closed.
     * Displays the closure reason in the TextView.
     *
     * @param code   The status code of the closure
     * @param reason The reason provided for closure
     */
    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
//            String s = msgTv.getText().toString();
//            msgTv.setText(s + "---\nconnection closed by " + closedBy + "\nreason: " + reason);
            appendLine("---");
            appendLine("[SYSTEM] Connection closed by " + closedBy);
            appendLine("Reason : " + reason);
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
       // appendLine("[SYSTEM] Connected to room: " + WebSocketManager.getInstance().getCurrentRoom());
        appendLine("[SYSTEM] Connected to " + courseName + " chat");
    }


    @Override
    public void onWebSocketError(Exception ex) {
        appendLine("[ERROR] " + ex.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatWebSocketManager.getInstance().disconnectWebSocket();
        ChatWebSocketManager.getInstance().removeWebSocketListener();
    }
}