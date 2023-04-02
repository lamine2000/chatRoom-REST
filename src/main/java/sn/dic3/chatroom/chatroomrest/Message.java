package sn.dic3.chatroom.chatroomrest;

public class Message {
    private String pseudo;
    private String content;

    public Message() {
    }

    public Message(String pseudo, String content) {
        this.pseudo = pseudo;
        this.content = content;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}