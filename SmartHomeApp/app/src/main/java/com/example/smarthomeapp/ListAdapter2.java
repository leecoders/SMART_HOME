package com.example.smarthomeapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


// DB에서 모드 정보 받아와야 함

public class ListAdapter2 extends BaseAdapter {

    TextView text;

    int getNum = MainActivity.num;

    // 토스트 중복 방지를 위함
    private static Toast sToast = null;
    public static void showToast(Context context, String message) {
        if (sToast == null) {
            sToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            sToast.setText(message);
        }
        sToast.show();
    }

    private Switch aSwitch = null;

    LayoutInflater inflater = null;
    private ArrayList<ItemData2> m_oData = null;
    private int nListCnt = 0;

    public ListAdapter2(ArrayList<ItemData2> _oData)
    {
        m_oData = _oData;
        nListCnt = m_oData.size();
    }

    @Override
    public int getCount()
    {
        Log.i("TAG", "getCount");
        return nListCnt;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    // 모드 수정
    public void modifyMode(String modeName, final ViewGroup parent) {
        Intent intent = new Intent(parent.getContext(), ModifyModeActivity.class);
        intent.putExtra("modeName", modeName);
        parent.getContext().startActivity(intent);
        ((Activity)parent.getContext()).finish(); // 메인 액티비티 종료하고 이동 -> 돌아올 때 다시 열어줘야함 (리스트뷰 갱신이 안되니까.. 일단 이 방법으로)
    }


    // DB에서 받아온 모드 전원 상태로 갱신해야 함
    @Override
    public View getView(final int pos, View convertView, final ViewGroup parent)
    {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (inflater == null)
            {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.listview2_item, parent, false);
        }

        final TextView oTextTitle = (TextView) convertView.findViewById(R.id.textTitle);

        oTextTitle.setText(m_oData.get(pos).strTitle);




        // 모드 이름 클릭하면 모드 정보 제공
        text = (TextView) convertView.findViewById(R.id.textTitle);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(parent.getContext(), m_oData.get(pos).strTitle + " clicked.");
            }
        });

        // 즉시 실행 버튼
        Button runRNButton = (Button) convertView.findViewById(R.id.runRNButton);
        runRNButton.setOnClickListener(new View.OnClickListener() {
            final Context context = parent.getContext();
            @Override
            public void onClick(View v) {
                String jsonData;
                try {
                    jsonData = new SendModeTask().execute(m_oData.get(pos).strTitle).get();
                    System.out.println(jsonData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    return;
                }
                // 실행 후에 디바이스 제어 상태가 바뀌기 때문에 화면 리셋해야 함
                // 어댑터2의 `즉시 실행` 버튼이 어댑터1을 새로고침해야 하므로 어쩔 수 없이 리스트뷰1과 어댑터1을 public static으로 지정함
//                MainActivity.setInitialModes();
                MainActivity.setInitialPowers();
                MainActivity.m_oListView.setAdapter(MainActivity.oAdapterTemp);

                showToast(parent.getContext(), m_oData.get(pos).strTitle + " 실행되었습니다.");
            }
        });

        // 모드 수정 버튼
        ImageButton modifyButton = (ImageButton) convertView.findViewById(R.id.modifyButton);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyMode(m_oData.get(pos).strTitle, parent);
            }
        });


        return convertView;
    }

    class SendModeTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                String configUrl = new Config().getSendUrl2();
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
}