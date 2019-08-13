package com.example.smarthomeapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

public class MainBackPressCloseHandler {

    private long backKeyPressedTime = 0;
    private String motion;

    private Activity activity;

    public MainBackPressCloseHandler(Activity context, String motion) {
        this.activity = context;
        this.motion = motion;
    }

    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        // 제목셋팅
//                alertDialogBuilder.setTitle("종료");

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage("모드 " + motion + "을 취소하시겠습니까?")
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
                                Intent intent = new Intent(activity, MainActivity.class);
                                activity.startActivity(intent);
                                activity.finish();
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }
}