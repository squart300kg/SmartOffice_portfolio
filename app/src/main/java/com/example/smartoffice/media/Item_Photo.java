package com.example.smartoffice.media;

import com.google.gson.annotations.SerializedName;

public class Item_Photo
{
    @SerializedName("image")
    String Photo;

    public String getPhoto()
    {
        return Photo;
    }

    public void setPhoto(String photo)
    {
        Photo = photo;
    }
}
