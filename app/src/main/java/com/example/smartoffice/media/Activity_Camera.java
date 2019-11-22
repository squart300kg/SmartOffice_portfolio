package com.example.smartoffice.media;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartoffice.MainActivity;
import com.example.smartoffice.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;


/**
 * todo: 스마트 오피스 CCTV 화면
 *
 * 액티비티 생성자: 김정화
 *
 * 작업내용
 * 1. 액티비티 xml, java 코드 작성
 * 2. 라즈베리 파이 카메라, 서보모터 연결
 * 3. 실시간 영상 송출받기
 * 4. 해당 액티비티에서 서보모터 제어
 *
 */

public class Activity_Camera extends AppCompatActivity
{
    String TAG = "Activity_Camera";
    WebView webView;
    Button leftBtn, rightBtn;
    ImageButton imgBtn_audioRecord, imgBtn_recording, imgBtn_stop, imgBtn_stoprecording, imgBtn_capture;
    TextView textView_audiorecording, door_state;
    LinearLayout door;

    // TCP 소켓연결 관련 변수
    Socket socket = null;
    DataInputStream in = null;
    DataOutputStream out = null;

    // 카운트다운
    CountDownTimer timer;
    int i = 11;

    // UDP 소켓 연결
//    DatagramSocket ds = null;

    // 음성 녹음 관련 변수
    public static MediaRecorder recorder;

    //    int audioSource = MediaRecorder.AudioSource.MIC;
//    int sampleRateInHz = 48000;
//    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
//    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
//    int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
//
//    byte Data[] = new byte[bufferSizeInBytes];
//
//    AudioRecord audioRecorder = new AudioRecord(audioSource,
//            sampleRateInHz,
//            channelConfig,
//            audioFormat,
//            bufferSizeInBy

    String IP = "192.168.0.58";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //도어락 제어 버튼
        door = findViewById(R.id.door);
        door_state = findViewById(R.id.door_state);

        door_state_initialize();
        door.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(door_state.getText().toString().equals("문열기"))
                {
                    //문을 열어준다.
                    door_state.setText("문닫기");
                    Toast.makeText(Activity_Camera.this, "문이 열렸습니다.", Toast.LENGTH_SHORT).show();
                    door_open();
                }
                else if (door_state.getText().toString().equals(("문닫기")))
                {
                    //문을 닫아준다.
                    door_state.setText("문열기");
                    Toast.makeText(Activity_Camera.this, "문이 닫혔습니다.", Toast.LENGTH_SHORT).show();
                    door_close();
                }
            }
        });
        // tcp 소켓 연결
        new NetworkTcpConnection().execute();

        // udp 소켓
//        try {
//            ds = new DatagramSocket();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebBridge(), "java");

        // set the scale 웹뷰 사이즈 조절
        webView.setInitialScale(320); // 330 == 330%

        String path = "http://" + IP + ":9999/javascript_simple.html";
        webView.loadUrl(path);

        webView.setPadding(0, 0, 0, 0);
//        webView.getSettings().setBuiltInZoomControls(false);
//        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setUseWideViewPort(true);

        // 카메라 앵글 왼쪽으로 이동
        leftBtn = findViewById(R.id.leftBtn);
        leftBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            out.writeUTF("left");
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        // 카메라 앵글 오른쪽으로 이동
        rightBtn = findViewById(R.id.rightBtn);
        rightBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            out.writeUTF("right");
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        textView_audiorecording = findViewById(R.id.textView_audiorecord);

        // 녹음 시작 ==> 녹음 완료되면 바로 스피커에서 재생되도록
        imgBtn_audioRecord = findViewById(R.id.imgBtn_audioRecord);
        imgBtn_audioRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(Activity_Camera.this, "녹음 시작", Toast.LENGTH_SHORT).show();

                // 버튼 visible 설정
                imgBtn_audioRecord.setVisibility(View.GONE);
                imgBtn_stop.setVisibility(View.VISIBLE);

                // textview 설정
                textView_audiorecording.setText("10초");

                // timer 설정
                timer = new CountDownTimer(10000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        i--;
                        textView_audiorecording.setText(i+"초");
                    }

                    @Override
                    public void onFinish() {
                        Log.d("hihi", "녹음 10초 다됐다");

                        // stop 버튼 누르기
                        imgBtn_stop.performClick();
                    }
                };

                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC); //----------- (1)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // -- (2)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // ------(3)
//                recorder.setMaxDuration(10000);     // 최대 10초동안 녹음 가능
                recorder.setOutputFile(Environment.getExternalStorageDirectory()+"/test.mp3");
                try {
                    recorder.prepare(); // -----------------------------------------------(4)
                } catch (IOException e) {
                    e.printStackTrace();
                }

                timer.start();
                recorder.start();   // Recording is now started ----------------------(5)


                // 화면전환 인텐트
//                Intent intent = new Intent(Activity_Camera.this, RecordingDialog.class);
//                startActivity(intent);


//                isRecording = true;
//                startRecording();
            }
        });

        imgBtn_stop = findViewById(R.id.imgBtn_stop);
        imgBtn_stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 11;
                timer.cancel();
                recorder.stop();
                imgBtn_audioRecord.setVisibility(View.VISIBLE);
                imgBtn_stop.setVisibility(View.GONE);
                textView_audiorecording.setText("최대 10초");

                // MP3 파일 생성됨 ==> 소켓으로 파일 전송해야됨
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            out.writeUTF("start");

                            File f = new File(Environment.getExternalStorageDirectory()+"/test.mp3");
                            FileInputStream fis = new FileInputStream(f);
                            BufferedInputStream bis = new BufferedInputStream(fis);

                            int len;
                            int size = 1024;
                            byte[] data = new byte[size];
                            while ((len = bis.read(data)) != -1) {
                                out.write(data, 0, len);
                            }

                            bis.close();
                            fis.close();

                            out.writeUTF("end");

                            Log.d("hihi", "파일 전송 완료");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });

        // 영상 녹화 시작 ==> 최대 녹화시간 30초 ~ 1분으로 제한할 것
        imgBtn_recording = findViewById(R.id.imgBtn_recording);
        imgBtn_recording.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                imgBtn_recording.setVisibility(View.GONE);
                imgBtn_stoprecording.setVisibility(View.VISIBLE);
                //                Toast.makeText(Activity_Camera.this, "영상 녹화 버튼 클릭", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            out.writeUTF("recordStart");

                            Log.d("hihi", "녹화 시작!!!!!!!!!!!!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });

        imgBtn_stoprecording = findViewById(R.id.imgBtn_stopRecording);
        imgBtn_stoprecording.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                imgBtn_recording.setVisibility(View.VISIBLE);
                imgBtn_stoprecording.setVisibility(View.GONE);

                Toast.makeText(Activity_Camera.this, "영상 녹화가 완료되었습니다", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            out.writeUTF("recordStop");

                            Log.d("hihi", "녹화 종료!!!!!!!!!!!!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });

        imgBtn_capture = findViewById(R.id.imgBtn_capture);
        imgBtn_capture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Activity_Camera.this, "사진이 저장되었습니다", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            out.writeUTF("capture");

                            Log.d("hihi", "캡처!!!!!!!!!!!!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });

    }

    private void door_state_initialize() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://54.180.2.52/door_state.php", // todo: php 파일 주소
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "onResponse: " + response);
                        if(response.equals("unlock"))
                        {
                            door_state.setText("문닫기");
                        }
                        else
                        {
                            door_state.setText("문열기");
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
        RequestQueue requestQueue = Volley.newRequestQueue(Activity_Camera.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    private void door_open() {
        String door_state = "unlock";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://54.180.2.52/door.php?door_state=" + door_state, // todo: php 파일 주소
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
        RequestQueue requestQueue = Volley.newRequestQueue(Activity_Camera.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }
    private void door_close() {
        String type = "manual";
        String door_state = "lock";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://54.180.2.52/door.php?door_state=" + door_state, // todo: php 파일 주소
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
        RequestQueue requestQueue = Volley.newRequestQueue(Activity_Camera.this);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

//    private void startRecord() throws IOException {
//        Log.d("hihi", "startRecord 시작");
//        File myfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "test.pcm");
//
//        myfile.createNewFile();
//
//
////        OutputStream outputStream = new FileOutputStream(myfile);
////        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
////
////        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
////
////        int minBufferSize = AudioRecord.getMinBufferSize(11025, 2, 2);
////
////        short[] audioData = new short[minBufferSize];
////
////        AudioRecord audioRecord = new AudioRecord(1, 11025, 2, 2, minBufferSize);
////
//////        audioRecord.getState();
//////        Log.d("jjjjjjjjj", audioRecord.getState() == 1 ? "아직 초기화 안됨" : "초기화 완료");
////
////        audioRecord.startRecording();
//
//        audioRecorder.startRecording();
//
//        while (recording) {
//            audioRecorder.read(Data, 0, Data.length);
//            audioRecorder.read(Data, 0, Data.length);
//            try {
//                os.write(Data, 0, bufferSizeInBytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                os.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
////            int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
////            for(int i=0; i<numberOfShort; i++) {
////                dataOutputStream.writeShort(audioData[i]);
////            }
//        }
//
//        // 저장소 관련 권한이 없으면 여기 진입하지 못하는 오류
//        if (!recording.booleanValue()) {
//            Log.d("hihi", "종료????");
//            audioRecord.stop();
//            dataOutputStream.close();
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        out.writeUTF("start");
//
//                        // 전송할 파일
//                        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");
//                        long fileSize = file.length();
//                        long totalReadBytes = 0;
//                        byte[] buffer = new byte[1024];
//                        int readBytes;
//
//                        FileInputStream fis = new FileInputStream(file);
//                        while ((readBytes = fis.read(buffer)) > 0) {
//                            out.write(buffer, 0, readBytes);
////                            String str = new String(buffer, "utf-8");
////                            out.writeUTF(str);
//                            totalReadBytes += readBytes;
//                        }
//
//                        Log.d("hihi", "fileSize : " + fileSize);
//                        Log.d("hihi", "totalReadBytes : " + totalReadBytes);
//
//                        out.writeUTF("end");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
//
//    }

//    public void startRecording()  {
//        audioRecorder.startRecording();
//        isRecording = true;
//        recordingThread = new Thread(new Runnable() {
//            public void run() {
////                String filepath = Environment.getExternalStorageDirectory().getPath();
//                Log.d("hihi", "startRecording 시작");
//                File myfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "record.pcm");
//
//                try {
//                    myfile.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                FileOutputStream os = null;
//                try {
////                    os = new FileOutputStream(filepath+"/record.pcm");
//                    os = new FileOutputStream(myfile);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//
//                while(isRecording) {
//                    audioRecorder.read(Data, 0, Data.length);
//                    try {
//                        os.write(Data, 0, bufferSizeInBytes);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        os.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if (!isRecording) {
//                    Log.d("hihi", "종료????");
//                    stopRecording();
//
//                    // 파일 전송하는 코드 넣기
//                }
//            }
//        });
//        recordingThread.start();
//    }
//
//    public void stopRecording() {
//        if (null != audioRecorder) {
//            isRecording = false;
//            audioRecorder.stop();
//            audioRecorder.release();
//            audioRecorder = null;
//            recordingThread = null;
//        }
//    }

    // 소켓 연결 스레드
    public class NetworkTcpConnection extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids)
        {
            // 소켓 연결 ==> Thread 사용 필요
            try
            {
                Log.d("hihi", "tcpConnection 시도");

                // 이미 연결된 소켓 객체가 있다면 / 또다른 소켓을 생성하지 않는다
                if (socket == null)
                {
                    // 로컬호스트
                    socket = new Socket(IP, 8080);
                    Log.d("hihi", "소켓들 처음 연결되어 객체 생성");

                    // 입력 / 출력 스트림 소켓에서 얻어온다
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                }

            } catch (IOException e)
            {
                Log.d("hihi", "터짐 UnknownHostException");
                e.printStackTrace();
            }

            return null;
        }
    }

    // 웹뷰 화면 사용시 필요
    class WebBridge
    {

        @JavascriptInterface
        public void call_log(final String _message)
        {
            Log.d("hihi", _message);
        }
    }
}