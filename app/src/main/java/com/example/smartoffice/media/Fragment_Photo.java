package com.example.smartoffice.media;

import android.content.Context;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Fragment_Photo extends Fragment
{
    private View View;
    private Context Context_Fragment_Photo;

    private RecyclerView mRecyclerView;
    private static List<Item_Photo> item_photo;
    private AdapterPhoto adapterPhoto;

    private String TAG = "Fragment_Photo";

    public Fragment_Photo()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View = inflater.inflate(R.layout.fragment_photo, container, false);
        Context_Fragment_Photo = getActivity().getApplicationContext();

        Log.e(TAG, "onCreateView: " );

        // todo: 리사이클러뷰 시작
        mRecyclerView = View.findViewById(R.id.recycler_view_photo);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(Context_Fragment_Photo));

        // 리스트 초기화 해주기
        item_photo = new ArrayList<>();

        // todo: 사진 목록 요청하기
        getPhotoList();

        return View;
    }

    // todo: 사진 목록 요청하기
    private void getPhotoList()
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
        Call<List<Item_Photo>> listCall = RequestPhoto.getPhoto("이이잉~ 앗살라 말라이쿰 ~ ");

        listCall.enqueue(new Callback<List<Item_Photo>>()
        {
            @Override
            public void onResponse(Call<List<Item_Photo>> call, Response<List<Item_Photo>> response)
            {
                Log.e(TAG, "list call onResponse = " + response.body());
                item_photo = response.body();

                // 넘어온 값 확인하기
                for (int i = 0; i < item_photo.size(); i++)
                {
                    Log.e(TAG, "onResponse: item_photo: " + item_photo.get(i).getPhoto());
                }

                adapterPhoto = new AdapterPhoto(Context_Fragment_Photo, item_photo);
                mRecyclerView.setAdapter(adapterPhoto);
            }

            @Override
            public void onFailure(Call<List<Item_Photo>> call, Throwable t)
            {
                Log.e(TAG, "onFailure id t: " + t.getMessage());
            }
        });
    }

    private class AdapterPhoto extends RecyclerView.Adapter<AdapterPhoto.ViewHolder>
    {

        public AdapterPhoto(Context context_Fragment_Photo, List<Item_Photo> itemphoto)
        {
            Context_Fragment_Photo = context_Fragment_Photo;
            item_photo = itemphoto;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(Context_Fragment_Photo).inflate(R.layout.item_photo, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {

            Picasso.get().load(item_photo.get(position).getPhoto()).
                    memoryPolicy(MemoryPolicy.NO_CACHE).
                    placeholder(R.drawable.ic_people).
                    networkPolicy(NetworkPolicy.NO_CACHE).
                    into(holder.photo);

            String result = item_photo.get(position).getPhoto().substring(item_photo.get(position).getPhoto().length()-26);
            Log.e(TAG, "onBindViewHolder: result: " + result);
            holder.photo_date_time.setText("CCTV가 촬영한 사진입니다\n" + result);
        }

        @Override
        public int getItemCount()
        {
            return item_photo.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public ImageView photo;
            public TextView photo_date_time;

            public ViewHolder(@NonNull android.view.View itemView)
            {
                super(itemView);

                photo = itemView.findViewById(R.id.photo);
                photo_date_time = itemView.findViewById(R.id.photo_date_time);
            }
        }
    }
}
