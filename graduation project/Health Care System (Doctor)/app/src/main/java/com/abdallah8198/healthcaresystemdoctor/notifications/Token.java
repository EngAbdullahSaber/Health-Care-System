package com.abdallah8198.healthcaresystemdoctor.notifications;

public class Token {
    /*An FCM Token ,or much commonly known as registerationToken
    . An ID issued by GSM
    connection servers to the client app allows it to recieve messages
    * */
    String token ;


    public Token() {
    }

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
