package com.ecosa.devicemovementtracker.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Utility {


  public static void configureToolbar(ActionBar actionBar, String title, boolean displayHomeButton) {
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(displayHomeButton);
    }

    public static String currentDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(c);
    }

    public static String formatToServer(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format.format(date);

    }

    public static String generateEncodedString(Bitmap bmp) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static boolean isDeviceConnected(Context context) {
        boolean isDeviceConnected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworks = null;
        if (connectivityManager != null) {
            activeNetworks = connectivityManager.getActiveNetworkInfo();
        }
        if (null != activeNetworks) {
            if (activeNetworks.getType() == ConnectivityManager.TYPE_MOBILE) {
                isDeviceConnected = true;
            }
            if (activeNetworks.getType() == ConnectivityManager.TYPE_WIFI) {
                isDeviceConnected = true;
            }
        }
        return isDeviceConnected;
    }

    public static void logMessage(Object model) {
        Log.e(Constants.TAG, new Gson().toJson(model));
    }

    public static void customLogMessage(Object model) {
        Log.d(Constants.TAG, new Gson().toJson(model));
    }

    public static void showNotification(View view, String message, int length) {
        Snackbar snackbar = Snackbar.make(view, message, length);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        TextView txtView = layout.findViewById(android.support.design.R.id.snackbar_text);
        txtView.setMaxLines(5);
        txtView.setTextColor(Color.parseColor("#FF4081"));
        snackbar.show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static View inflateView(Context context, int resource) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(resource, null);
    }


    public static String toLongDateString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return format.format(date);

    }

    public static String formatDate(String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm aaa", Locale.getDefault());
        Date date = null;
        //check for the milliseconds part
        int position = dateFormat.lastIndexOf(".");
        if (position < 0) {
            dateFormat = dateFormat + ".000";
        }
        try {
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            date = serverFormat.parse(dateFormat);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return format.format(date);
    }

    public static String formatDbDate(String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm aaa", Locale.getDefault());
        Date date = null;
        try {
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            date = serverFormat.parse(dateFormat);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return format.format(date);
    }

    private static int getColors(int index) {
        String[] colors = {"#469408", "#e69a2a", "#FF4081", "#414980", "#469408"};
        if (colors.length > (index - 1))
            return Color.parseColor(colors[index - 1]);
        return Color.parseColor(colors[0]);

    }






}
