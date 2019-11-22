package com.example.smartoffice.retrifit_setup;



import com.example.smartoffice.media.Item_Photo;
import com.example.smartoffice.media.Item_Video;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface
{
    // 사진 목록 불러오기
    @FormUrlEncoded
    @POST("getImage.php")
    Call<List<Item_Photo>> getPhoto(@Field("") String eeeeeeeing);

    // 동영상 목록 불러오기
    @FormUrlEncoded
    @POST("getVideo.php")
    Call<List<Item_Video>> getVideo(@Field("") String eeasdasd);
}


