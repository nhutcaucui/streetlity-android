package com.streetlity.client;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class UpdateLocationService extends Service implements LocationListener{
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.streetlity.client.action.FOO";
    private static final String ACTION_BAZ = "com.streetlity.client.action.BAZ";

    private static final String EXTRA_PARAM1 = "com.streetlity.client.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.streetlity.client.extra.PARAM2";

    static final int NOTIFICATION_ID = 543;

    public static boolean isServiceRunning = false;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpdateLocationService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpdateLocationService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    LocationManager locationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction().equals("start")) {
            startServiceWithNotification();
        }
        else stopMyService();
        return START_STICKY;
    }

    void startServiceWithNotification() {
        if (isServiceRunning) return;
        isServiceRunning = true;

        Log.e("tag", "startServiceWithNotification: ");
//        Intent notificationIntent = new Intent(getApplicationContext(), MyActivity.class);
//        notificationIntent.setAction(C.ACTION_MAIN);  // A string containing the action name
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.my_icon);
//
//        Notification notification = new NotificationCompat.Builder(this)
//                .setContentTitle(getResources().getString(R.string.app_name))
//                .setTicker(getResources().getString(R.string.app_name))
//                .setContentText(getResources().getString(R.string.my_string))
//                .setSmallIcon(R.drawable.my_icon)
//                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
//                .setContentIntent(contentPendingIntent)
//                .setOngoing(true)
////                .setDeleteIntent(contentPendingIntent)  // if needed
//                .build();
//        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;     // NO_CLEAR makes the notification stay when the user performs a "delete all" command
//        startForeground(NOTIFICATION_ID, notification);
    }

    void stopMyService() {
        //stopForeground(true);
        Log.e("", "stopMyService: " );
        stopSelf();
        isServiceRunning = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 1000, this);
        }


        Log.d("myTag", "Service onCreate()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("tag", "onDestroy: " );
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call2 = tour.updateLocation(MyApplication.getInstance().getUsername(),
                (float) location.getLatitude(), (float) location.getLongitude());
        call2.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        Log.e("tag", "onResponse: " + new JSONObject(response.body().string()));
//                        Handler handler = new Handler(Looper.getMainLooper());
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    Toast.makeText(UpdateLocationService.this, "This is my message: "
//                                            , Toast.LENGTH_LONG).show();
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("tag", "onResponse: " + response.code());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        //locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
