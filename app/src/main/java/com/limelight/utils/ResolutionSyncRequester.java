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

    static int port = 8080;

    public static void setResolution(Context context, String host, int refreshRate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean disableResolutionSync = !prefs.getBoolean("checkbox_enable_resolution_sync", false);
        if (disableResolutionSync) {
            return;
        }

        setMouseSpeed(host, Integer.parseInt(prefs.getString("pointer_speed", "5")));
        setScrollLines(host, Integer.parseInt(prefs.getString("scroll_wheel_lines", "3")));

        Boolean enabled = prefs.getBoolean("checkbox_should_sync_resolution", false);
        if (!enabled) {
            setRefreshRate(host, refreshRate);
            return;
        }

        int width = Integer.parseInt(prefs.getString("sync_width", "1920"));
        int height = Integer.parseInt(prefs.getString("sync_height", "1080"));

        makeRequest(String.format("http://%s:%d/resolutionsync/set?%d&%d&%d", host, port, width, height, refreshRate));


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Boolean disableMouseAcceleration = prefs.getBoolean("checkbox_disable_pointer_precision", false);
                if (disableMouseAcceleration) {
                    disableMouseAcceleration(host);
                }
            }
        }, 1000);
    }

    public static void resetResolution(Context context, String host) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean disableResolutionSync = !prefs.getBoolean("checkbox_enable_resolution_sync", false);
        if (disableResolutionSync) {
            return;
        }

        makeRequest(String.format("http://%s:%d/resolutionsync/reset", host, port));
    }

    static void setRefreshRate(String host, int refreshRate) {
        makeRequest(String.format("http://%s:%d/resolutionsync/setRefreshRate?%d", host, port, refreshRate));
    }

    static void disableMouseAcceleration(String host) {
        makeRequest(String.format("http://%s:%d/resolutionsync/disableMouseAcceleration", host, port));
    }

    static void setMouseSpeed(String host, int speed) {
        makeRequest(String.format("http://%s:%d/resolutionsync/setMouseSpeed?%d", host, port, speed));
    }

    static void setScrollLines(String host, int lines) {
        makeRequest(String.format("http://%s:%d/resolutionsync/setScrollLines?%d", host, port, lines));
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
