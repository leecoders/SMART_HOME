package com.example.smarthomeapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class AddModeActivity extends AppCompatActivity {

    private static Toast sToast = null;
    EditText modeText;
    Button saveButton, cancelButton;
    Switch aSwitch[] = new Switch[5];
    String modeName;
    String deviceName[] = {"valve", "cleaner", "light", "blind", "windows"};
    int deviceVal[] = {0, 0, 0, 0, 0};
//    Map<String, Integer> checkDevices;

    final Context context = this;
    private MainBackPressCloseHandler mainBackPressCloseHandler;

    // 백 키 리스너
    @Override
    public void onBackPressed() {
        mainBackPressCloseHandler.onBackPressed();
    }

    public void createMode() {
        // 디바이스 스위치 상태 받아오기
        modeName = modeText.getText().toString(); // 모드 이름 받아오기
        for(int i=0; i < deviceVal.length; ++i) {
            if(aSwitch[i].isChecked())
                deviceVal[i] = 1;
            else
                deviceVal[i] = 0;
        }

        // 서버로 모드 생성 요청
        System.out.println("CreateModeTask 시도 ...");
        String resultText = "값이 없음";

        try {
            resultText = new CreateModeTask().execute().get();
            System.out.println("뽈스가 오면 중복 : " + resultText);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(resultText.equals("true")) {
            Intent intent = new Intent(AddModeActivity.this, MainActivity.class);
            AddModeActivity.this.startActivity(intent);
            AddModeActivity.this.finish();
        }
        else {
            sToast.makeText(getApplicationContext(), "이미 존재하는 모드 이름입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mode);

        // 백 키 핸들러
        mainBackPressCloseHandler = new MainBackPressCloseHandler(this, "생성");

        // 저장 클릭 시
        saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 모드 추가
                createMode();
            }
        });

        // 취소 클릭 시
        cancelButton = (Button)findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // 제목셋팅
//                alertDialogBuilder.setTitle("종료");

                // AlertDialog 셋팅
                alertDialogBuilder
                        .setMessage("모드 생성을 취소하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        // 다이얼로그를 취소한다
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton("예",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        // 액티비티를 종료한다
                                        Intent intent = new Intent(AddModeActivity.this, MainActivity.class);
                                        AddModeActivity.this.startActivity(intent);
                                        AddModeActivity.this.finish();
                                    }
                                });

                // 다이얼로그 생성
                AlertDialog alertDialog = alertDialogBuilder.create();

                // 다이얼로그 보여주기
                alertDialog.show();
            }
        });

        // 모드 이름, 디바이스 스위치들 id 받기
        modeText = (EditText)findViewById(R.id.modeName);
        aSwitch[0] = (Switch)findViewById(R.id.switch1);
        aSwitch[1] = (Switch)findViewById(R.id.switch2);
        aSwitch[2] = (Switch)findViewById(R.id.switch3);
        aSwitch[3] = (Switch)findViewById(R.id.switch4);
        aSwitch[4] = (Switch)findViewById(R.id.switch5);

    }

    class CreateModeTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                String configUrl = new Config().getCreateModeUrl();
                URL url = new URL(configUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("mode_nm", modeName);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                for(int i=0; i < deviceVal.length; ++i) {
                    try {
                        jsonData.put(deviceName[i], deviceVal[i]);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

//                sendMsg = "device_nm="+strings[0]+"&device_val="+strings[1];
                osw.write("ModeInfo="+jsonData.toString());
                osw.flush();
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode()+"에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }


}