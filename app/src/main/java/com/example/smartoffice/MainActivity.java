package com.example.smartoffice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartoffice.media.Activity_Camera;
import com.example.smartoffice.media.Activity_Media;
import com.example.smartoffice.service.MyFirebaseInstanceIDService;
import com.example.smartoffice.service.MyFirebaseMessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.speech.*;

/**
 * todo: 스마트 오피스 홈화면
 * 팀원
 * - 김성훈, 김정화, 송상윤
 * <p>
 * 액티비티 생성자: 김성훈
 * <p>
 * 작업내용
 * 1. 레이아웃 화면 구성
 */

public class MainActivity extends AppCompatActivity
{
    private String TAG = "MainActivity";
    static int PERMISSION = 1;

    // View
    private TextView button_camera, tv_dust_concentration, tv_humidity, tv_temperature,
            fan_switch_on,
            fan_switch_off, // 선풍기 on / off 버튼

            humidifier_switch_on,
            humidifier_switch_off, // 가습기 on / off 버튼

            tv_people_count, // 실내에 입장한 인원수 표시
            auto_off,
            auto_on,
            button_media; // 다시보기

    private LinearLayout fan_button, // 선풍기 on / off 버튼
            humidifier_button, // 가습기 on / off 버튼
            mic_button_fan_and_humidity, // 음성인식 버튼 (선풍기 / 가습기 전원(on / off)을 음성으로 제어하기)
            manual,
            auto_button;

    private ImageView button_refresh_dust, button_refresh_humidity, button_refresh_people_count;

    private boolean fan_status = false // 선풍기 전원 상태
            , humidity_status = false // 가습기 전원 상태
            , auto_status = false;

    private FrameLayout mic_anim_area; // 마이크 애니메이션 화면

    private LottieAnimationView lottieMicAnimView;

    private SpeechRecognizer mRecognizer;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 퍼미션 허용 요청
        if (Build.VERSION.SDK_INT >= 23)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET,
                            Manifest.permission.RECORD_AUDIO,               // 음성인식
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},    // 외부 저장소
                    PERMISSION);
        }

        // 서비스에서 Firebase 푸쉬 알림 대기
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(new Intent(MainActivity.this, MyFirebaseInstanceIDService.class));
//        } else {
//            startService(new Intent(MainActivity.this, MyFirebaseInstanceIDService.class));
//        }

        Intent MyFirebaseInstanceIDService = new Intent(MainActivity.this, MyFirebaseInstanceIDService.class);
        startService(MyFirebaseInstanceIDService);

        Intent MyFirebaseMessagingService = new Intent(MainActivity.this, com.example.smartoffice.service.MyFirebaseMessagingService.class);
        startService(MyFirebaseMessagingService);

        // todo: ViewFind
        button_camera = findViewById(R.id.button_camera); // 방범 카메라
        button_refresh_dust = findViewById(R.id.button_refresh_dust); // 새로고침 버튼 _ 사무실 주변 미세먼지
        button_refresh_humidity = findViewById(R.id.button_refresh_humidity); // 새로고침 _ 사무실 내부 습도
        tv_dust_concentration = findViewById(R.id.tv_dust_concentration); // 미세먼지 농도 표시
        tv_humidity = findViewById(R.id.tv_humidity); // 습도 표시
        tv_temperature = findViewById(R.id.tv_temperature); // 온도 표시

        // 선풍기 제어
        fan_button = findViewById(R.id.fan_button);
        fan_switch_on = findViewById(R.id.fan_switch_on);
        fan_switch_off = findViewById(R.id.fan_switch_off);

        // 가습기 제어
        humidifier_button = findViewById(R.id.humidifier_button);
        humidifier_switch_on = findViewById(R.id.humidifier_switch_on);
        humidifier_switch_off = findViewById(R.id.humidifier_switch_off);

        // 음성인식 관련 뷰 (선풍기, 가습기)
        mic_button_fan_and_humidity = findViewById(R.id.mic_button_fan_and_humidity);
        mic_anim_area = findViewById(R.id.mic_anim_area);
        lottieMicAnimView = findViewById(R.id.lottieMicAnimView);

        // 입장 인원수 관련 뷰
        tv_people_count = findViewById(R.id.tv_people_count);
        button_refresh_people_count = findViewById(R.id.button_refresh_people_count);
        manual = findViewById(R.id.manual);
        auto_button = findViewById(R.id.auto_button);
        auto_on = findViewById(R.id.auto_on);
        auto_off = findViewById(R.id.auto_off);

        // 다시보기 버튼
        button_media = findViewById(R.id.button_media);

        /* viewFind 끝 */

        tv_humidity = findViewById(R.id.tv_humidity);

        // todo: 방범 카메라 화면으로 이동하기
        button_camera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, Activity_Camera.class);
                startActivity(intent);
            }
        });

        // 앱 실행하면
        // todo: 미세먼지 값 불러오기
        getDustResult();

        // todo: 사무실 내부 온습도 정보 불러오기
        gethumidityAndtempurterResult();

        // todo: 새로고침 버튼 모음
        refreshGroup();

        // todo: (온습도 센서 메소드) 온습도 정보 받기 && 가습기, 선풍기 전원 관련 제어
        Temperature_and_humidity();

        // todo : 사무실 안 인원수 불러오기
        getPeopleCount();

        // 다시보기 버튼
        button_media.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, Activity_Media.class);
                startActivity(intent);
            }
        });
        // 음성인식 버튼
        mic_button_fan_and_humidity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e(TAG, "onClick: 음성인식 버튼 클릭함");

                mic_anim_area.setVisibility(View.VISIBLE);

                //사용자에게 음성을 요구하고 음성 인식기를 통해 전송하는 활동을 시작합니다.
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                //음성 인식을위한 음성 인식기의 의도에 사용되는 여분의 키입니다.
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                //음성을 번역할 언어를 설정합니다.
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

                // 애니메이션 재생
                lottieMicAnimView.setAnimation("listen-state.json");
                lottieMicAnimView.setRepeatCount(LottieDrawable.INFINITE);
                lottieMicAnimView.playAnimation();

                mRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
                mRecognizer.setRecognitionListener(listener);
                mRecognizer.startListening(intent);
            }
        });
    }



    private void auto_off()
    {
        String type = "manual";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://squart300kg.cafe24.com/iot/controll_application_setAuto.php?type=" + type, // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "onResponse: " + response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    private void auto_on()
    {
        String type = "auto";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://squart300kg.cafe24.com/iot/controll_application_setAuto.php?type=" + type, // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "onResponse: " + response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    private void fan_off()
    {
        String application = "fan";
        String controll = "off";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://squart300kg.cafe24.com/iot/controll_application.php?application=" + application + "&controll=" + controll, // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "onResponse: " + response);

                        tv_dust_concentration.setText(response);

                        // 버튼 활성화
                        button_refresh_dust.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    private void fan_on()
    {
        String application = "fan";
        String controll = "on";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://squart300kg.cafe24.com/iot/controll_application.php?application=" + application + "&controll=" + controll, // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "onResponse: " + response);

                        tv_dust_concentration.setText(response);

                        // 버튼 활성화
                        button_refresh_dust.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    private void humid_on()
    {
        String application = "humid";
        String controll = "on";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://squart300kg.cafe24.com/iot/controll_application.php?application=" + application + "&controll=" + controll, // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "onResponse: " + response);

                        tv_dust_concentration.setText(response);

                        // 버튼 활성화
                        button_refresh_dust.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    private void humid_off()
    {
        String application = "humid";
        String controll = "off";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://squart300kg.cafe24.com/iot/controll_application.php?application=" + application + "&controll=" + controll, // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "onResponse: " + response);

                        tv_dust_concentration.setText(response);

                        // 버튼 활성화
                        button_refresh_dust.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    // todo: 새로고침 버튼 모음
    private void refreshGroup()
    {
        // todo: 사무실 주변 미세먼지 정보 새로고침 버튼 클릭하기
        button_refresh_dust.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                // 새로고침 버튼 클릭하면 미세먼지 값 불러오기
                getDustResult();

                // 버튼 중복클릭 방지
                button_refresh_dust.setVisibility(View.GONE);
            }
        });

        // todo: 사무실 내부 온습도 정보 새로고침 버튼 클릭하기
        button_refresh_humidity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // do something

                // todo: 사무실 내부 온습도 정보 불러오기
                gethumidityAndtempurterResult();
            }
        });


        // todo: 사무실 내부 입장 인원수 새로고침
        button_refresh_people_count.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                button_refresh_people_count.setVisibility(View.GONE);
                tv_people_count.setText(" ");
                getPeopleCount();

            }
        });
    }

    /**
     * UI
     **/

    // todo: 사무실 내부 온습도 정보 불러오기
    private void gethumidityAndtempurterResult()
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://squart300kg.cafe24.com/iot/select_humid_temp.php", // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        try // JSON 형태로 결과 요청을 받는다.
                        {
                            Log.e(TAG, "onResponse: " + response);

                            // 버튼 활성화
                            button_refresh_people_count.setVisibility(View.VISIBLE);

                            // json 인식하기
                            JSONObject jsonObject = new JSONObject(response);

                            String success = jsonObject.getString("success"); // = 1
                            Log.e(TAG, "onResponse: jsonObject.getString success = " + success);

                            JSONArray jsonArray = jsonObject.getJSONArray("read");

                            if (success.equals("1"))
                            {
                                for (int i = 0; i < jsonArray.length(); i++)
                                {
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    String humid = object.getString("humid").trim();
                                    String temp = object.getString("temp").trim();

                                    // todo: 서버에서 불러온 json 값 세팅

                                    // 온도 숫자만 표시하기 (%)
                                    tv_humidity.setText(humid);

                                    // 온도 숫자만 표시하기
                                    tv_temperature.setText(temp);
                                }
                            } else
                            {
                                Toast.makeText(MainActivity.this, "JsonArrayError.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e)
                        {

                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    // todo: (온습도 센서 메소드 ) 온습도rk 정보 받기 && 가습기, 선풍기 전원 관련 제어
    void Temperature_and_humidity()
    {
        // todo : 자동 제어 상태 체크
        if (auto_status == true)
        {
            manual.setVisibility(View.INVISIBLE);
            // 전원 켜짐 표시
            auto_on.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕
            auto_off.setTextColor(Color.parseColor("#ffffff")); // 흰 글씨

            auto_off.setTextColor(Color.parseColor("#000000")); // 검은 글씨
            auto_off.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마

//            Log.e(TAG, "Temperature_and_humidity: 자동 제어 on");

        } else
        {
            manual.setVisibility(View.VISIBLE);
            // 전원 꺼짐 표시
            auto_off.setBackgroundColor(Color.parseColor("#000000")); // 검은 글씨
            auto_off.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마

            auto_off.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕
            auto_off.setTextColor(Color.parseColor("#ffffff")); // 흰 글씨

//            Log.e(TAG, "Temperature_and_humidity: 자동 제어 off");

        }

        // todo: 자동제어 전원 on/ off 버튼
        auto_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (auto_status == true)
                {
                    // 전원 꺼짐
                    manual.setVisibility(View.VISIBLE);
                    auto_on.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마
                    auto_on.setTextColor(Color.parseColor("#000000")); // 검은 바탕

                    auto_off.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕
                    auto_off.setTextColor(Color.parseColor("#ffffff")); // 흰 글씨

                    auto_status = false;

                    Log.e(TAG, "onClick: 자동 제어 전원 off");
                    auto_off();
                } else
                {
                    // 전원 켜짐
                    manual.setVisibility(View.INVISIBLE);
                    auto_on.setTextColor(Color.parseColor("#ffffff")); // 검은 글씨
                    auto_on.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕

                    auto_off.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마
                    auto_off.setTextColor(Color.parseColor("#000000")); // 검은 글씨

                    auto_status = true;

                    Log.e(TAG, "onClick: 자동 제어 전원 on");
                    auto_on();
                }
            }
        });
        // todo: 자동제어 전원 상태 체크
        if (fan_status == true)
        {
            // 전원 켜짐 표시
            fan_switch_on.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕
            fan_switch_on.setTextColor(Color.parseColor("#ffffff")); // 흰 글씨

            fan_switch_off.setTextColor(Color.parseColor("#000000")); // 검은 글씨
            fan_switch_off.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마

            Log.e(TAG, "Temperature_and_humidity: 선풍기 전원 on");

        } else
        {
            // 전원 꺼짐 표시
            fan_switch_on.setBackgroundColor(Color.parseColor("#000000")); // 검은 글씨
            fan_switch_on.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마

            fan_switch_off.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕
            fan_switch_off.setTextColor(Color.parseColor("#ffffff")); // 흰 글씨

            Log.e(TAG, "Temperature_and_humidity: 선풍기 전원 off");

        }

        // todo: 가습기 전원 상태 체크
        if (humidity_status == true)
        {
            // 전원 켜짐 표시
            humidifier_switch_on.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕
            humidifier_switch_on.setTextColor(Color.parseColor("#ffffff")); // 흰 글씨

            humidifier_switch_off.setTextColor(Color.parseColor("#000000")); // 검은 글씨
            humidifier_switch_off.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마
        } else
        {
            // 전원 꺼짐 표시
            humidifier_switch_on.setTextColor(Color.parseColor("#000000")); // 검은 글씨
            humidifier_switch_on.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마

            humidifier_switch_off.setBackgroundColor(Color.parseColor("#000000")); // 흰 바탕 바탕
            humidifier_switch_off.setTextColor(Color.parseColor("#ffffff")); // 검은 글씨
        }


        // todo: 선풍기 전원 on/ off 버튼
        fan_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (fan_status == true)
                {
                    // 전원 꺼짐
                    fan_switch_on.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마
                    fan_switch_on.setTextColor(Color.parseColor("#000000")); // 검은 바탕

                    fan_switch_off.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕
                    fan_switch_off.setTextColor(Color.parseColor("#ffffff")); // 흰 글씨

                    fan_status = false;

                    Log.e(TAG, "onClick: 선풍기 전원 off");
                    fan_off();
                } else
                {
                    // 전원 켜짐
                    fan_switch_on.setTextColor(Color.parseColor("#ffffff")); // 검은 글씨
                    fan_switch_on.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕

                    fan_switch_off.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마
                    fan_switch_off.setTextColor(Color.parseColor("#000000")); // 검은 글씨

                    fan_status = true;

                    Log.e(TAG, "onClick: 선풍기 전원 on");
                    fan_on();
                }
            }
        });

        // todo: 가습기 전원 on/ off 버튼
        humidifier_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (humidity_status == true)
                {
                    // 전원 꺼짐
                    humidifier_switch_on.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마
                    humidifier_switch_on.setTextColor(Color.parseColor("#000000")); // 검은 바탕

                    humidifier_switch_off.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕
                    humidifier_switch_off.setTextColor(Color.parseColor("#ffffff")); // 흰 글씨

                    humidity_status = false;

                    Log.e(TAG, "onClick: 가습기 전원 off");
                    humid_off();
                } else
                {
                    // 전원 켜짐
                    humidifier_switch_on.setTextColor(Color.parseColor("#ffffff")); // 검은 글씨
                    humidifier_switch_on.setBackgroundColor(Color.parseColor("#000000")); // 검은 바탕

                    humidifier_switch_off.setBackground(getDrawable(R.drawable.item_switch_theme)); // 꺼짐 테마
                    humidifier_switch_off.setTextColor(Color.parseColor("#000000")); // 검은 글씨

                    humidity_status = true;

                    Log.e(TAG, "onClick: 가습기 전원 on");
                    humid_on();
                }
            }
        });
    }

    // todo: 음성인식 메소드
    private RecognitionListener listener = new RecognitionListener()
    {
        @Override
        public void onReadyForSpeech(Bundle params)
        {
//            Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onReadyForSpeech: ");
        }

        @Override
        public void onBeginningOfSpeech()
        {
            Log.e(TAG, "onBeginningOfSpeech: ");
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
            Log.e(TAG, "onRmsChanged: ");
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {
            Log.e(TAG, "onBufferReceived: ");
        }

        @Override
        public void onEndOfSpeech()
        {
            Log.e(TAG, "onEndOfSpeech: ");
        }

        @Override
        public void onError(int error)
        {
            String message;

            switch (error)
            {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    mic_anim_area.setVisibility(View.GONE);
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    mic_anim_area.setVisibility(View.GONE);
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    mic_anim_area.setVisibility(View.GONE);
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    mic_anim_area.setVisibility(View.GONE);
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    mic_anim_area.setVisibility(View.GONE);
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    mic_anim_area.setVisibility(View.GONE);
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    mic_anim_area.setVisibility(View.GONE);
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    mic_anim_area.setVisibility(View.GONE);
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    mic_anim_area.setVisibility(View.GONE);
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    mic_anim_area.setVisibility(View.GONE);
                    break;
            }

            Log.e(TAG, "onError: message: " + message);
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
        }

        // todo: 입력된 음성 -> 텍스트 확인하기 (STT 결과 확인하기)
        @Override
        public void onResults(Bundle results)
        {
            mic_anim_area.setVisibility(View.GONE);

            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            Log.e(TAG, "onResults: matches.size: " + matches.size());

            for (int i = 0; i < matches.size(); i++)
            {
//                Log.e(TAG, "onResults: " + matches.get(i));
//                Toast.makeText(MainActivity.this, matches.get(i), Toast.LENGTH_SHORT).show();

                Log.e(TAG, "onResults: " + matches.get(i));
            }

            // matches 리스트의 0번 인덱스에 담긴 값만 출력한다
            String result = matches.get(0);
            if (result.contains("가습기") && result.contains("선풍기") && result.contains("켜"))
            {
                fan_on();
                humid_on();
                Toast.makeText(MainActivity.this, "선풍기 ON\n가습기 ON", Toast.LENGTH_SHORT).show();
            } else if (result.contains("가습기") && result.contains("선풍기") && result.contains("꺼"))
            {
                humid_off();
                fan_off();
                Toast.makeText(MainActivity.this, "선풍기 OFF\n가습기 OFF", Toast.LENGTH_SHORT).show();
            } else if (result.contains("선풍기") && result.contains("켜"))
            {
                fan_on();
                Toast.makeText(MainActivity.this, "선풍기 ON", Toast.LENGTH_SHORT).show();
            } else if (result.contains("선풍기") && result.contains("꺼"))
            {
                fan_off();
                Toast.makeText(MainActivity.this, "선풍기 OFF", Toast.LENGTH_SHORT).show();
            } else if (result.contains("가습기") && result.contains("켜"))
            {
                humid_on();
                Toast.makeText(MainActivity.this, "가습기 ON", Toast.LENGTH_SHORT).show();
            } else if (result.contains("가습기") && result.contains("꺼"))
            {
                humid_off();
                Toast.makeText(MainActivity.this, "가습기 OFF", Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(MainActivity.this, "다시 한번 더 말씀해 주세요", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {
        }
    };

    // todo: 미세먼지 값 불러오기
    void getDustResult()
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://54.180.2.52/Select_dust.php", // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "onResponse: " + response);

                        tv_dust_concentration.setText(response);

                        // 버튼 활성화
                        button_refresh_dust.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    private void getPeopleCount()
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://squart300kg.cafe24.com/iot/select_people_count.php", // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "onResponse: " + response);

                        tv_people_count.setText(response);

                        // 버튼 활성화
                        button_refresh_people_count.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }
}
