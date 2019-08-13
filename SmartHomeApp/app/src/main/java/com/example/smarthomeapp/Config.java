package com.example.smarthomeapp;

public class Config {
    public static String dir = "http://192.168.0.19:8080/";
    public static String recieveUrl = dir + "deviceRead.jsp";
    public static String sendUrl = dir + "deviceStateUpdate.jsp";
    public static String recieveUrl2 = dir + "modeRead.jsp";
    public static String sendUrl2 = dir + "modeExecute.jsp";
    public static String createModeUrl = dir + "modeCreate.jsp";
    public static String deleteModeUrl = dir + "modeDelete.jsp";
    public static String updateModeUrl = dir + "modeUpdate.jsp";

    public static String getReceiveUrl() {
        return recieveUrl;
    }

    public static String getSendUrl() {
        return sendUrl;
    }

    public static String getReceiveUrl2() {
        return recieveUrl2;
    }

    public static String getSendUrl2() {
        return sendUrl2;
    }

    public static String getCreateModeUrl() {
        return createModeUrl;
    }

    public static String getDeleteModeUrl() {
        return deleteModeUrl;
    }

    public static String getUpdateModeUrl() {
        return updateModeUrl;
    }
}

