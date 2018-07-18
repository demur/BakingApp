package com.udacity.demur.bakingapp.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RecipeStep implements Serializable {
    @Expose
    @SerializedName("thumbnailURL")
    private String thumbnailURL;
    @Expose
    @SerializedName("videoURL")
    private String videoURL;
    @Expose
    @SerializedName("description")
    private String description;
    @Expose
    @SerializedName("shortDescription")
    private String shortDescription;
    @Expose
    @SerializedName("id")
    private int id;

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}