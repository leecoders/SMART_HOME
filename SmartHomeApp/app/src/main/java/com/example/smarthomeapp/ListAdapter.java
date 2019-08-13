package com.example.smarthomeapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
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


// DB에서 디바이스 정보 받아와야 함

public class ListAdapter extends BaseAdapter {

    String[] device = {"가스밸브", "공기청정기", "전등", "블라인드", "창문"}; // 디바이스 목록. 모드 목록도 추가해야함
    String[] deviceEng = {"VALVE", "CLEANER", "LIGHT", "BLIND", "WINDOWS"}; // 디바이스 목록. 모드 목록도 추가해야함
//    int[] deviceState = {0, 0, 0, 0, 0};

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
    private ArrayList<ItemData> m_oData = null;
    private int nListCnt = 0;

    public ListAdapter(ArrayList<ItemData> _oData)
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


    // 디바이스 ON
    public void powerOn(int pos) {
        System.out.println(pos + " on 시도 ...");
        String resultText = "값이 없음";

        try {
            resultText = new SendTask().execute(deviceEng[pos], "1").get();
            System.out.println(resultText);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    // 디바이스 OFF
    public void powerOff(int pos) {
        System.out.println(pos + " off 시도 ...");
        String resultText = "값이 없음";

        try {
            resultText = new SendTask().execute(deviceEng[pos], "0").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    // DB에서 불러온 ON/OFF 상태 반영해줘야 함
    @Override
    public View getView(final int pos, View convertView, final ViewGroup parent) {

        if (convertView == null) {
            final Context context = parent.getContext();
            if (inflater == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }


        // ON/OFF 스위치 ==============================
        aSwitch = (Switch) convertView.findViewById(R.id.switch1);

        // 초기 스위치 세팅
//        setInitialPowers(); // 서버에서 전원 상태 받아옴. 근데 여기있으면 디바이스 개수만큼 호출됨
        if(MainActivity.deviceState[pos] == 1) {
            aSwitch.setChecked(true);
        }
        else {
            aSwitch.setChecked(false);
        }

        // aSwitch.setChecked(); // DB에서 불러온 ON/OFF 상태 반영해줘야 함
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) { // switch on
                    powerOn(pos);
                    showToast(parent.getContext(), m_oData.get(pos).strTitle + " ON");
                }
                else { // switch off
                    powerOff(pos);
                    showToast(parent.getContext(), m_oData.get(pos).strTitle + " OFF");
                }
            }
        });

        TextView oTextTitle = (TextView) convertView.findViewById(R.id.textTitle);

        oTextTitle.setText(m_oData.get(pos).strTitle);
        return convertView;
    }

    class SendTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                String configUrl = new Config().getSendUrl();
                URL url = new URL(configUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "device_nm="+strings[0]+"&device_val="+strings[1];
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