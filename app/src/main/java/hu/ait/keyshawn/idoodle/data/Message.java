package hu.ait.keyshawn.idoodle.data;

/**
 * Created by mac on 5/18/17.
 */

public class Message {
    String sender;
    String body;
    public Boolean isSystem;

    public Message(String sender, String body) {
        this.sender = sender;
        this.body = body;
        this.isSystem = false;
    }

    Message() {}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
