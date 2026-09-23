package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;

/*
* Class for chat messages
*
* -sender name
* -room name
* -message text
* -system messages
 */

public class ChatMessage {
    public String type; //chat or system
    public String sender; //name of sender
    public String room; //room name
    public String content; //message text
    public long timestamp;

    public ChatMessage(String type, String sender, String room, String content, long timestamp) {
        this.type = type;
        this.sender = sender;
        this.room = room;
        this.content = content;
        this.timestamp = timestamp;
    }

    /*
    * Converts chatmessage into JSON text
     */
    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", type);
            obj.put("sender", sender);
            obj.put("room", room);
            obj.put("content", content);
            obj.put("timestamp", timestamp);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
    //test
    /*
    * Parses JSON text received into chatmessage obj
     */
    public static ChatMessage fromJSON(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);

        String type = obj.optString("type", "chat");
        String sender = obj.optString("sender", "Unknown");
        String room = obj.optString("room", "general");
        String content = obj.optString("content", "");
        long timestamp = obj.optLong("timestamp", System.currentTimeMillis());

        return new ChatMessage(type, sender, room, content, timestamp);
    }
}
