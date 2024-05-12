package com.nagyd.tidder.model;

public class Post {
    public String title;
    public String author;
    public String desc;
    public String parent;
    public String id;

    public Post() {};

    public Post(String title, String author, String desc, String parent, String id) {
        this.title = title;
        this.author = author;
        this.desc = desc;
        this.parent = parent;
        this.id = id;
    }
}
