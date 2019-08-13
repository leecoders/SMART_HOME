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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class ModifyModeActivity extends AppCompatActivity {

    private static Toast sToast = null;
    TextView textView;
    Button saveButton, deleteButton;
    public String modeName;
    EditText editText;
    Switch aSwitch[] = new Switch[5];
    static String[] deviceState = new String[5];
    String deviceName[] = {"valve", "cleaner", "light", "blind", "windows"};

    final Context context = this;
    private MainBackPressCloseHandler mainBackPressCloseHandler;

    // 백 키 리스너
    @Override
    public void onBackPressed() {
        mainBackPressCloseHandler.onBackPressed();
    }

    public void deleteMode() {
        String resultText = "값이 없음";
        try {
            resultText = new DeleteModeTask().execute(modeName).get();
            System.out.println(modeName + "삭제 시도 : " + resultText);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(resultText.equals("true")) {
            // 액티비티를 종료한다
            Intent intent = new Intent(ModifyModeActivity.this, MainActivity.class);
            ModifyModeActivity.this.startActivity(intent);
            ModifyModeActivity.this.finish();
            sToast.makeText(getApplicationContext(), "해당 모드가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        }
        else {
            sToast.makeText(getApplicationContext(), "모드 삭제 실패", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveMode() {
        String resultText = "값이 없음";

        try {
            resultText = new UpdateModeTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("종범이의 리스폰스 결과는 ??? : " + resultText);

        if(resultText.equals("true")) {
            Intent intent = new Intent(ModifyModeActivity.this, MainActivity.class);
            ModifyModeActivity.this.startActivity(intent);
            ModifyModeActivity.this.finish();
        }
        else {
            sToast.makeText(getApplicationContext(), "수정 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void setDeviceStatesAfterCheck() {
        for(int i=0; i < deviceState.length; ++i) {
            if(aSwitch[i].isChecked()) {
                deviceState[i] = "1";
            }
            else {
                deviceState[i] = "0";
            }
        }
    }

    public void setDeviceStates() {
        String jsonData;
        try {
            jsonData = new ReceiveModeTask().execute().get();
            System.out.println(jsonData);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            JSONArray jsonArr = (JSONArray)jsonObject.get("modeList");
            System.out.println("modeList : " + jsonArr);

            for(int i=0; i<jsonArr.length(); ++i) {
                JSONObject jsonName = (JSONObject)jsonArr.get(i);
                String modeNm = (String)jsonName.get("mode_nm");
                if(modeNm.equals(modeName)) {
                    deviceState[0] = jsonName.get("valve").toString();
                    deviceState[1] = jsonName.get("cleaner").toString();
                    deviceState[2] = jsonName.get("light").toString();
                    deviceState[3] = jsonName.get("blind").toString();
                    deviceState[4] = jsonName.get("windows").toString();
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_mode);

        // 어댑터2에서 모드이름 받아오기
        modeName = getIntent().getStringExtra("modeName");

        // 뷰에서 ID 받아오기
        editText = (EditText)findViewById(R.id.modeName);
        editText.setText(modeName);
        aSwitch[0] = (Switch)findViewById(R.id.switch1);
        aSwitch[1] = (Switch)findViewById(R.id.switch2);
        aSwitch[2] = (Switch)findViewById(R.id.switch3);
        aSwitch[3] = (Switch)findViewById(R.id.switch4);
        aSwitch[4] = (Switch)findViewById(R.id.switch5);

        // 서버에서 받아온 해당 모드 정보 반영
        setDeviceStates();
        for(int i=0; i < deviceState.length; ++i) {
            if(deviceState[i].equals("1")) { // 자바는 equals로 문자열 비교해야 함
                aSwitch[i].setChecked(true);
            }
        }

        // 백 키 핸들러
        mainBackPressCloseHandler = new MainBackPressCloseHandler(this, "수정");

        // 저장 클릭 시
        saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 수정은 모드이름 중복 확인 필요 없음
                setDeviceStatesAfterCheck();
                saveMode();
            }
        });

        // 취소 클릭 시
        deleteButton = (Button)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                // 제목셋팅
//                alertDialogBuilder.setTitle("종료");

                // AlertDialog 셋팅
                alertDialogBuilder
                        .setMessage(modeName + "을 삭제하시겠습니까?")
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
                                        // 모드를 삭제한다.
                                        deleteMode();
                                    }
                                });

                // 다이얼로그 생성
                AlertDialog alertDialog = alertDialogBuilder.create();

                // 다이얼로그 보여주기
                alertDialog.show();
            }
        });
    }

    class UpdateModeTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                String configUrl = new Config().getUpdateModeUrl();
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

                for(int i=0; i < deviceState.length; ++i) {
                    try {
                        jsonData.put(deviceName[i], deviceState[i]);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

//                sendMsg = "device_nm="+strings[0]+"&device_val="+strings[1];
                osw.write("ModeUpdate="+jsonData.toString());
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

    class DeleteModeTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                String configUrl = new Config().getDeleteModeUrl();
                URL url = new URL(configUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                sendMsg = "mode_nm="+strings[0];
                osw.write(sendMsg);
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

    public static class ReceiveModeTask extends AsyncTask<String, Void, String> {

        String clientKey = "#########################";
        private String str, receiveMsg;
        private final String ID = "########";

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            try {
                String configUrl = new Config().getReceiveUrl2();
                url = new URL(configUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                conn.setRequestProperty("x-waple-authorization", clientKey);

                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                    Log.i("receiveMsg : ", receiveMsg);

                    reader.close();
                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
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
