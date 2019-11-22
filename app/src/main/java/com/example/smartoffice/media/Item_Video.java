package com.example.smartoffice.media;

import com.google.gson.annotations.SerializedName;

public class Item_Video
{
    @SerializedName("video")
    String VideoRoot;

    public String getVideoRoot()
    {
        return VideoRoot;
    }

    public void setVideoRoot(String videoRoot)
    {
        VideoRoot = videoRoot;
    }
}
