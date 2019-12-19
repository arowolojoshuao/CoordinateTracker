package com.ecosa.devicemovementtracker.Util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



public class SettingsMain {
    public static final String PREF_NAME = "devicetracker";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int NETWORK_STATUS_NOT_CONNECTED = 2, NETWORK_STAUS_WIFI = 1, NETWORK_STATUS_MOBILE = 0;
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 0;
    public static int TYPE_NOT_CONNECTED = 2;
    private static Dialog dialog1;
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;

    // Constructor
    @SuppressLint("CommitPrefEdits")
    public SettingsMain(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /* Checking -Internet Connection */
    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {

                        return true;
                    }
        }
        return false;
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
                activeNetwork = cm.getActiveNetworkInfo();
                Log.d("info d ", activeNetwork.getType() + "" + activeNetwork.getTypeName());
            }
        }


        if (null != activeNetwork) {
            Log.d("info adssad", "adasd");
            if (activeNetwork.getType() == TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static int getConnectivityStatusString(Context context) {
        Log.d("info d", getConnectivityStatus(context) + "");
        int conn = getConnectivityStatus(context);
        int status = 0;
        if (conn == TYPE_WIFI) {
            status = NETWORK_STAUS_WIFI;
        } else if (conn == TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE;
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = NETWORK_STATUS_NOT_CONNECTED;
        }
        return status;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), decoded, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(Context inContext, Uri uri) {
        @SuppressLint("Recycle") Cursor cursor = inContext.getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                return cursor.getString(idx);

            }
        } catch (Exception e) {
            Log.d("info GetReal Path Error", e.toString());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            } catch (Exception ex) {
                Log.d("info GetReal Path Error", ex.toString());

            }
        }

        return "";
    }

    public static void reload(Context context, String tag) {
        Fragment frg;
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();

        frg = manager.findFragmentByTag(tag);
        final FragmentTransaction ft = manager.beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }

    public static boolean isSocial(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String restoredText = pref.getString("isSocial", "false");
        return restoredText.equals("true");
    }

    public static Uri decodeFile(Context context, File f) {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//
//       File destFile = new File(file, "img_"
//                + dateFormatter.format(new Date()).toString() + ".png");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), b, "Title", null);
        return Uri.parse(path);
    }

//    public static void adforest_changeRattingBarcolor(RatingBar ratingBar, Context context) {
//        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
//        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);
//        stars.getDrawable(1).setColorFilter(context.getResources().getColor(R.color.gradientfifth), PorterDuff.Mode.SRC_ATOP);
//        stars.getDrawable(0).setColorFilter(context.getResources().getColor(R.color.rattingBarColor), PorterDuff.Mode.SRC_ATOP);
//
//    }

    public static String getUserId() {
        return pref.getString("login", "0");
    }

    public static String getLanguageCode() {
        return pref.getString("languageCode", "");
    }

    public static void setLanguageCode(String languageCode) {
        editor.putString("languageCode", languageCode);
        editor.commit();
    }

    public static permissionsModel getPermissionsModel() {


        Gson gson = new Gson();
        String permissionsModel = pref.getString("permissionsModel", null);
        permissionsModel model = gson.fromJson(permissionsModel, permissionsModel.class);
        return model;
    }
//
    public static void setPermissionsModel(permissionsModel permissionsModel) {
        Gson gson = new Gson();
        String toJson = gson.toJson(permissionsModel);
        editor.putString("permissionsModel", toJson);
        editor.apply();

    }

//    public static void setProgressModel(ProgressModel progressModel) {
//        Gson gson = new Gson();
//        String toJson = gson.toJson(progressModel);
//        editor.putString("progressModel", toJson);
//        editor.apply();
//
////    }
//
//    public static ProgressModel getProgressSettings(Context context) {
//
//
//        Gson gson = new Gson();
//        String progressModel = pref.getString("progressModel", null);
//        ProgressModel model = gson.fromJson(progressModel, ProgressModel.class);
//        return model;
//    }

    public static String getMainColor() {
        return pref.getString("mainColor", "#000000");
    }

    public void setMainColor(String mainColor) {
        editor.putString("mainColor", mainColor);
        editor.commit();
    }

    public String getAlertDialogMessage(String type) {
        return pref.getString(type, "You Are Not Connected To Internet. Please Check Your Internet Connection and Try Again");
    }

    public void setAlertDialogMessage(String type, String msg) {
        editor.putString(type, msg);
        editor.commit();
    }

    public String getKey(String name) {
        return pref.getString(name, "");
    }

    public void setKey(String name, String value) {
        editor.putString(name, value);
        editor.commit();
    }

    public String getAlertDialogTitle(String type) {
        return pref.getString(type, "Error");
    }

    public void setAlertDialogTitle(String type, String title) {
        editor.putString(type, title);
        editor.commit();
    }

    public String getAlertOkText() {
        return pref.getString("AlertOkText", "OK");
    }

    public void setAlertOkText(String msg) {
        editor.putString("AlertOkText", msg);
        editor.commit();
    }

    public String getAlertCancelText() {
        return pref.getString("AlertCancelText", "CANCEL");
    }

    public void setAlertCancelText(String msg) {
        editor.putString("AlertCancelText", msg);
        editor.commit();
    }

    public void setUserPhone(String userPhone) {
        editor.putString("phone", userPhone);
        editor.commit();
    }

    public String getUserImage() {
        return pref.getString("image", "0");
    }

    public void setUserImage(String userImage) {
        editor.putString("image", userImage);
        editor.commit();
    }

    public String getUserLogin() {
        return pref.getString("login", "0");
    }

    public void setUserLogin(String userLogin) {
        editor.putString("login", userLogin);
        editor.commit();
    }

    public String getUserName() {
        return pref.getString("username", "UserName");
    }

    public void setUserName(String userName) {
        editor.putString("username", userName);
        editor.commit();

    }

    public String getUserEmail() {
        return pref.getString("useremail", "UserEmail");
    }

    public void setUserEmail(String userEmail) {
        editor.putString("useremail", userEmail);
        editor.commit();

    }

    public String getUserPassword() {
        return pref.getString("userPassword", "0");
    }

    public void setUserPassword(String UserPassword) {
        editor.putString("userPassword", UserPassword);
        editor.commit();
    }

    public boolean getAdsShow() {
        return pref.getBoolean("showAd", false);
    }

    public void setAdsShow(boolean value) {
        editor.putBoolean("showAd", value);
        editor.commit();
    }

    public boolean getInterstitalShow() {
        return pref.getBoolean("is_show_initial", false);
    }

    public void setInterstitalShow(boolean value) {
        editor.putBoolean("is_show_initial", value);
        editor.commit();
    }

    public boolean getBannerShow() {
        return pref.getBoolean("is_show_banner", false);
    }

    public void setBannerShow(boolean value) {
        editor.putBoolean("is_show_banner", value);
        editor.commit();
    }

    public void setAdsPosition(String value) {
        editor.putString("adsPosition", value);
        editor.commit();
    }

    public String getAdsPostion() {
        return pref.getString("adsPosition", "adsPosition");
    }

    public String getBannerAdsId() {
        return pref.getString("banner_id", "");
    }

    public void setBannerAdsId(String value) {
        editor.putString("banner_id", value);
        editor.commit();
    }

    public String getInterstitialAdsId() {
        return pref.getString("interstital_id", "");
    }

    public void setInterstitialAdsId(String value) {
        editor.putString("interstital_id", value);
        editor.commit();
    }

    public int getAdsInitialTime() {
        return Integer.parseInt(pref.getString("time_initial", "30"));
    }

    public void setAdsInitialTime(String value) {
        editor.putString("time_initial", value);
    }

    public int getAdsDisplayTime() {
        return Integer.parseInt(pref.getString("time", "30"));
    }

    public void setAdsDisplayTime(String value) {
        editor.putString("time", value);
    }

    public boolean getAnalyticsShow() {
        return pref.getBoolean("AnalyticsShow", false);
    }

    public void setAnalyticsShow(boolean value) {
        editor.putBoolean("AnalyticsShow", value);
        editor.commit();
    }

    public String getAnalyticsId() {
        return pref.getString("analyticsId", "");
    }

    public void setAnalyticsId(String analyticsId) {
        editor.putString("analyticsId", analyticsId);
        editor.commit();
    }

    public void setGoogleButn(boolean value) {
        editor.putBoolean("googleButton", value);
        editor.commit();
    }

    public boolean getGooglButn() {
        return pref.getBoolean("googleButton", false);
    }

    public void setfbButn(boolean value) {
        editor.putBoolean("fbButton", value);
        editor.commit();
    }

    public boolean getfbButn() {
        return pref.getBoolean("fbButton", false);
    }

    public String getFireBaseId() {
        return pref.getString("firebaseid", "");
    }

    public void setFireBaseId(String value) {
        editor.putString("firebaseid", value);
        editor.commit();
    }

    public boolean getRTL() {
        return pref.getBoolean("RTL", false);
    }

    public void setRTL(boolean value) {
        editor.putBoolean("RTL", value);
        editor.commit();
    }

    public String getYoutubeApi() {
        return pref.getString("youTubeApi", "");
    }

    public void setYoutubeApi(String value) {
        editor.putString("youTubeApi", value);
        editor.commit();
    }

    public String getGenericAlertTitle() {
        return pref.getString("title", "Confirm!");
    }

    public void setGenericAlertTitle(String title) {
        editor.putString("title", title);
        editor.commit();
    }

    public String getGenericAlertMessage() {
        return pref.getString("text", "Are You Sure You Want To Do This!");
    }

    public void setGenericAlertMessage(String title) {
        editor.putString("text", title);
        editor.commit();
    }

    public String getGenericAlertOkText() {
        return pref.getString("btn_ok", "OK");
    }

    public void setGenericAlertOkText(String title) {
        editor.putString("btn_ok", title);
        editor.commit();
    }

    public String getGenericAlertCancelText() {
        return pref.getString("btn_no", "Cancel");
    }

    public void setGenericAlertCancelText(String title) {
        editor.putString("btn_no", title);
        editor.commit();
    }

    public void isAppOpen(boolean appOpen) {
        editor.putBoolean("app_open", appOpen);
        editor.commit();
    }

    public boolean getAppOpen() {
        return pref.getBoolean("app_open", false);
    }

    public void checkOpen(boolean appOpen) {
        editor.putBoolean("checkOpen", appOpen);
        editor.commit();
    }

    public String getGuestImage() {
        return pref.getString("guest_image", "");
    }

    public void setGuestImage(String message) {
        editor.putString("guest_image", message);
        editor.commit();
    }

    public boolean getCheckOpen() {
        return pref.getBoolean("checkOpen", false);
    }

    public String getNoLoginMessage() {
        return pref.getString("noLoginmessage", "Please login to perform this action.");
    }

    public void setNoLoginMessage(String message) {
        editor.putString("noLoginmessage", message);
        editor.commit();
    }

    public boolean isFeaturedScrollEnable() {
        return pref.getBoolean("featured_scroll_enabled", false);
    }

    public void setFeaturedScrollEnable(boolean featuredScrollEnable) {
        editor.putBoolean("featured_scroll_enabled", featuredScrollEnable);
        editor.commit();
    }

    public int getFeaturedScroolDuration() {
        return pref.getInt("featured_duration", 40);

    }

    public void setFeaturedScroolDuration(int duration) {
        editor.putInt("featured_duration", duration);
        editor.commit();
    }

    public int getFeaturedScroolLoop() {
        return pref.getInt("featured_loop", 40);

    }

    public void setFeaturedScroolLoop(int duration) {
        editor.putInt("featured_loop", duration);
        editor.commit();
    }


    //region LocationPopup

    public String getAppLogo() {
        return pref.getString("appLogo", "");

    }

    public void setAppLogo(String appLogo) {
        editor.putString("appLogo", appLogo);
        editor.commit();
    }

    public void setPaymentCompletedMessage(String paymentCompletedMessage) {
        editor.putString("message", paymentCompletedMessage);
        editor.commit();
    }

    public String getpaymentCompletedMessage() {
        return pref.getString("message", "Order Places Succ");
    }

    public String getGpsTitle() {
        return pref.getString("gpsTitle", "GPS AppCompatPreferenceActivity");
    }

    public void setGpsTitle(String gpsTitle) {
        editor.putString("gpsTitle", gpsTitle);
        editor.commit();
    }

    public String getGpsText() {
        return pref.getString("gpsText", "GPS is not enabled. Do you want to go to settings menu?");
    }

    public void setGpsText(String gpsText) {
        editor.putString("gpsText", gpsText);
        editor.commit();
    }

    public String getGpsConfirm() {
        return pref.getString("gpsConfirm", "AppCompatPreferenceActivity");
    }

    public void setGpsConfirm(String gpsConfirm) {
        editor.putString("gpsConfirm", gpsConfirm);
        editor.commit();
    }

    public String getGpsCancel() {
        return pref.getString("gpsCancel", "Clear");
    }

    public void setGpsCancel(String gpsCancel) {
        editor.putString("gpsCancel", gpsCancel);
        editor.commit();
    }

    public void setShowNearby(boolean b) {
        editor.putBoolean("show_nearby", b);
        editor.commit();
    }

    public boolean getShowNearBy() {
        return pref.getBoolean("show_nearby", false);
    }

    public boolean getAdsPositionSorter() {
        return pref.getBoolean("ads_position_sorter", false);
    }

    public void setAdsPositionSorter(boolean b) {
        editor.putBoolean("ads_position_sorter", b);
        editor.commit();
    }

    public String getLatitude() {
        return pref.getString("nearby_latitude", "");
    }

    public void setLatitude(String latitude) {
        editor.putString("nearby_latitude", latitude);
        editor.commit();
    }

    //endregion

    public String getLongitude() {
        return pref.getString("nearby_longitude", "");
    }

    public void setLongitude(String longitude) {
        editor.putString("nearby_longitude", longitude);
        editor.commit();
    }

    public String getDistance() {
        return pref.getString("nearby_distance", "");
    }

    public void setDistance(String longitude) {
        editor.putString("nearby_distance", longitude);
        editor.commit();
    }

    public String getNotificationTitle() {
        return pref.getString("notificationTitle", "");
    }

    public void setNotificationTitle(String notificationTitle) {
        editor.putString("notificationTitle", notificationTitle);
        editor.commit();
    }

    public String getNotificationMessage() {
        return pref.getString("notificationMessage", "");
    }

    public void setNotificationMessage(String notificationMessage) {
        editor.putString("notificationMessage", notificationMessage);
        editor.commit();
    }

    public String getNotificationImage() {
        return pref.getString("notificationImage", "");
    }

    public void setNotificationImage(String notificationImage) {
        editor.putString("notificationImage", notificationImage);
        editor.commit();
    }

    public String getLanguageName() {
        return pref.getString("languageName", "");
    }

    public void setLanguageName(String languageName) {
        editor.putString("languageName", languageName);
        editor.commit();
    }

    public boolean getLanguageRtl() {
        return pref.getBoolean("languageRtl", false);
    }

    public void setLanguageRtl(boolean b) {
        editor.putBoolean("languageRtl", b);
        editor.commit();
    }

    public boolean isLanguageChanged() {
        return pref.getBoolean("isLanguageChanged", true);
    }

    public void setLanguageChanged(boolean b) {
        editor.putBoolean("isLanguageChanged", b);
        editor.commit();
    }

    public boolean isAdShowOrNot() {
        return pref.getBoolean("setShowAd", true);
    }

    public void setAdShowOrNot(boolean b) {
        editor.putBoolean("setShowAd", b);
        editor.commit();
    }

    public String getShopUrl() {

        return pref.getString("app_page_test_url", "");
    }

    public void setShopUrl(String set) {
        editor.putString("app_page_test_url", set);
        editor.commit();
    }

//    public void setLocationPopupModel(Location_popupModel locationPopupModel) {
//        Gson gson = new Gson();
//        String toJson = gson.toJson(locationPopupModel);
//        editor.putString("locationPopupModel", toJson);
//        editor.apply();
//
//    }

//    public Location_popupModel getLocationPopupModel(Context context) {
//        Gson gson = new Gson();
//        String progressModel = pref.getString("locationPopupModel", null);
//        Location_popupModel model = gson.fromJson(progressModel, Location_popupModel.class);
//        return model;
//    }

//    public void setAdPostImageModel(AdPostImageModel AdPostImageModel) {
//        Gson gson = new Gson();
//        String toJson = gson.toJson(AdPostImageModel);
//        editor.putString("AdPostImageModel", toJson);
//        editor.apply();
//
//    }

//    public AdPostImageModel getAdPostImageModel(Context context) {
//        Gson gson = new Gson();
//        String progressModel = pref.getString("AdPostImageModel", null);
//        AdPostImageModel model = gson.fromJson(progressModel, AdPostImageModel.class);
//        return model;
//    }
//
//    public ArrayList<shopMenuModel> getShopMenu() {
//        Type listOfBecons = new TypeToken<List<shopMenuModel>>() {}.getType();
//
//        ArrayList<shopMenuModel> shop_menu = new Gson().fromJson(pref.getString("shop_menu", ""), listOfBecons);
//        return shop_menu;
//    }
//
//    public void setShopMenu(ArrayList<shopMenuModel> shopListModes) {
//        Type type = new TypeToken<List<shopMenuModel>>() {}.getType();
//
//        String strBecons = new Gson().toJson(shopListModes, type);
//        pref.edit().putString("shop_menu", strBecons).apply();
//    }

}