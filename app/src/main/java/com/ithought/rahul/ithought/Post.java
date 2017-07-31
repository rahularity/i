package com.ithought.rahul.ithought;

/**
 * Created by Rahul on 7/21/2017.
 */

public class Post {

    private String title;
    private String ImageUrl;

    public Post(){}

    public Post(String title, String imageUrl) {
        this.title = title;
        ImageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return ImageUrl;
    }
}
