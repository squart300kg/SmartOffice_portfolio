package com.example.smartoffice.media;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smartoffice.R;
import com.example.smartoffice.retrifit_setup.ApiClient;
import com.example.smartoffice.retrifit_setup.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_Video extends Fragment
{
    private View View;
    private Context Context_Fragment_Video;

    private RecyclerView mRecyclerView;
    private static List<Item_Video> item_video;
    private AdapterVideo adapterVideo;

    private String TAG = "Fragment_Photo";

    public static String VIDEO_ROOT;

    public Fragment_Video()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View = inflater.inflate(R.layout.fragment_video, container, false);
        Context_Fragment_Video = getActivity().getApplicationContext();

        Log.e(TAG, "onCreateView: " );

        // todo: 리사이클러뷰 시작
        mRecyclerView = View.findViewById(R.id.recycler_view_video);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(Context_Fragment_Video));

        // 리스트 초기화 해주기
        item_video = new ArrayList<>();

        // todo: 영상 목록 요청하기
        getVideoList();

        return View;
    }

    // todo: 영상 목록 요청하기
    private void getVideoList()
    {
        Log.e(TAG, "getPhotoList: 사진목록 조회 시작");

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //Defining retrofit api service
        ApiInterface RequestPhoto = retrofit.create(ApiInterface.class);

        //defining the call
        Call<List<Item_Video>> listCall = RequestPhoto.getVideo("");

        listCall.enqueue(new Callback<List<Item_Video>>()
        {
            @Override
            public void onResponse(Call<List<Item_Video>> call, Response<List<Item_Video>> response)
            {
                Log.e(TAG, "list call onResponse = " + response.body());
                item_video = response.body();

                // 넘어온 값 확인하기
                for (int i = 0; i < item_video.size(); i++)
                {
                    Log.e(TAG, "onResponse: item_videoRoot: " + item_video.get(i).getVideoRoot());
                }

                adapterVideo = new AdapterVideo(Context_Fragment_Video, item_video);
                mRecyclerView.setAdapter(adapterVideo);
            }

            @Override
            public void onFailure(Call<List<Item_Video>> call, Throwable t)
            {
                Log.e(TAG, "onFailure id t: " + t.getMessage());
            }
        });

    }

    private class AdapterVideo extends RecyclerView.Adapter<AdapterVideo.ViewHolder>
    {

        public AdapterVideo(Context context_Fragment_Video, List<Item_Video> itemVideo)
        {
            Context_Fragment_Video = context_Fragment_Video;
            item_video = itemVideo;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(Context_Fragment_Video).inflate(R.layout.item_video, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position)
        {

            String result = item_video.get(position).getVideoRoot().substring(item_video.get(position).getVideoRoot().length()-15);
            Log.e(TAG, "onBindViewHolder: result: " + result);
            holder.video_date_time.setText("CCTV가 촬영한 영상입니다\n파일명: " + result);

            holder.view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(android.view.View v)
                {
                    VIDEO_ROOT = item_video.get(position).getVideoRoot();

                    Intent intent = new Intent(Context_Fragment_Video, Activity_Video_Player.class);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return item_video.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public View view;
            public TextView video_date_time;

            public ViewHolder(@NonNull android.view.View itemView)
            {
                super(itemView);

                view = itemView;
                video_date_time = itemView.findViewById(R.id.video_date_time);
            }
        }
    }
}
