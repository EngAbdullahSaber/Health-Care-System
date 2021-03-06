package com.abdallah8198.healthcaresystemdoctor.models;

public class ModelUsers {
    //using same name as in firebase
    String name , phone ,email ,search ,image ,cover , uid  ,kind;

    public ModelUsers(){


    }

    public ModelUsers(String name, String phone, String email, String search, String image, String cover, String uid, String kind) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.search = search;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
