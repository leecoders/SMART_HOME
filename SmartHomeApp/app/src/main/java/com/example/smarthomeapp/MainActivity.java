package com.example.smarthomeapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

// 메인 액티비티로 DB에서 디바이스, 모드 이름 가져와야 함

public class MainActivity extends AppCompatActivity {
    public static ListView m_oListView = null;
    public static ListView m_oListView2 = null;
    public static ListAdapter oAdapterTemp; // 어댑터2로 어댑터1을 전달하기 위한 임시 변수
    public static ListAdapter2 oAdapterTemp2;

    public static int num = 3;
    static int[] deviceState = {0, 0, 0, 0, 0};
    int deviceLen = 5; // 현재 { 창문, 블라인드, 전등, 공기청정기, 가스밸브 } 로 5개 밖에 없음
    String[] device = {"가스밸브", "공기청정기", "전등", "블라인드", "창문"}; // 디바이스 목록. 모드 목록도 추가해야함
    static ArrayList<String> modeName = new ArrayList<String>();
//    ArrayList<Integer> modeVal = new ArrayList<Integer>();


    // 토스트 중복 방지를 위함
    public static Toast sToast = null;
    public static void showToast(Context context, String message) {
        if (sToast == null) {
            sToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            sToast.setText(message);
        }
        sToast.show();
    }

    // 초기 스위치 설정(DB 반영)
    public static void setInitialPowers() {
        String jsonData;
        try {
            jsonData = new ReceiveTask().execute().get();
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
            JSONArray jsonArr =  (JSONArray)jsonObject.get("deviceState");
            System.out.println("deviceState : " + jsonArr);

            for(int i=0; i<jsonArr.length(); ++i) {
                JSONObject jsontemp = (JSONObject)jsonArr.get(i);
                System.out.println("deviceTemp : " + jsontemp);
                String deviceNm = (String)jsontemp.get("device_nm"); // 얘는 필요 없는 듯
                int deviceVal = (Integer)jsontemp.get("device_val");
                deviceState[i] = deviceVal;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 초기 스위치 설정(DB 반영)
    public static void setInitialModes() {
        modeName.clear();
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
            JSONArray jsonArr =  (JSONArray)jsonObject.get("modeList");
            System.out.println("modeList : " + jsonArr);

            for(int i=0; i<jsonArr.length(); ++i) {
                JSONObject jsonName = (JSONObject)jsonArr.get(i);
                String modeNm = (String)jsonName.get("mode_nm");
                modeName.add(modeNm);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 모드 추가 클릭 시
    public void addMode() {
        Intent intent = new Intent(this, AddModeActivity.class);
        startActivity(intent);
        MainActivity.this.finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // fab
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMode();
            }
        });

        setInitialPowers();


        // 첫 번째 리스트 뷰
        ArrayList<ItemData> oData = new ArrayList<>();
        for (int i=0; i < deviceLen; ++i) // 디바이스 개수가 필요함
        {
            ItemData oItem = new ItemData();
            oItem.strTitle = device[i]; // 디바이스 이름이 들어갈 자리
            oData.add(oItem);
        }

        // ListView, Adapter 생성 및 연결 ------------------------
        m_oListView = (ListView)findViewById(R.id.listView);
        ListAdapter oAdapter = new ListAdapter(oData);
        m_oListView.setAdapter(oAdapter);


        oAdapterTemp = oAdapter;

        // 모드 먼저 받아오기
        setInitialModes();

        // 두 번째 리스트 뷰
        ArrayList<ItemData2> oData2 = new ArrayList<>();
        for (int i=0; i < modeName.size(); ++i) // 모드 개수가 필요함
        {
            ItemData2 oItem = new ItemData2();
            oItem.strTitle = modeName.get(i); // 모드 이름이 들어갈 자리
            oData2.add(oItem);
        }

        // ListView, Adapter 생성 및 연결 ------------------------
        m_oListView2 = (ListView)findViewById(R.id.listView2);
        ListAdapter2 oAdapter2 = new ListAdapter2(oData2);
        m_oListView2.setAdapter(oAdapter2);

        oAdapterTemp2 = oAdapter2;

    }

    public static class ReceiveTask extends AsyncTask<String, Void, String> {

        String clientKey = "#########################";
        private String str, receiveMsg;
        private final String ID = "########";

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            try {
                String configUrl = new Config().getReceiveUrl();
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
