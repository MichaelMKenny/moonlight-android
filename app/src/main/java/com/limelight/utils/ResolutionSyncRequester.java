package com.limelight.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ResolutionSyncRequester {

    static int port = 48020;

    public static void setResolution(Context context, String host, int refreshRate, Boolean isResume) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean disableResolutionSync = !prefs.getBoolean("checkbox_enable_resolution_sync", false);
        if (disableResolutionSync) {
            return;
        }

        int pointerSpeed = Integer.parseInt(prefs.getString("pointer_speed", "5"));
        int scrollLines = Integer.parseInt(prefs.getString("scroll_wheel_lines", "3"));

        String url = String.format("http://%s:%d/set?fps=%d&is_resume=%d&mouse_speed=%d&scroll_lines=%d", host, port, refreshRate, isResume ? 1 : 0, pointerSpeed, scrollLines);

        if (prefs.getBoolean("checkbox_should_sync_resolution", false)) {
            int width = Integer.parseInt(prefs.getString("sync_width", "1920"));
            int height = Integer.parseInt(prefs.getString("sync_height", "1080"));
            url += String.format("&width=%d&height=%d", width, height);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (prefs.getBoolean("checkbox_disable_pointer_precision", false)) {
                    makeRequest(String.format("http://%s:%d/set?mouse_acceleration=0", host, port));
                }
            }
        }, 1000);

        makeRequest(url);
    }

    public static void resetResolution(Context context, String host) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("checkbox_enable_resolution_sync", false)) {
            return;
        }

        makeRequest(String.format("http://%s:%d/reset", host, port));
    }


    static void makeRequest(String urlString) {
        try {
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int status = connection.getResponseCode();
            if (status > 299) {
                Log.e("ResolutionSyncRequester", String.format("Failed fetching %s: %d", url.toString(), status));
            }

            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("ResolutionSyncRequester", e.toString());
        }
    }
}
