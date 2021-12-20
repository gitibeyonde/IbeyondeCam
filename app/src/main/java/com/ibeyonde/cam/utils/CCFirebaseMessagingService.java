package com.ibeyonde.cam.utils;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.R;
import com.ibeyonde.cam.ui.login.LoginViewModel;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CCFirebaseMessagingService  extends FirebaseMessagingService {
    private static final String TAG= CCFirebaseMessagingService.class.getCanonicalName();

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);
            Log.d(TAG, "From: " + remoteMessage.getFrom());

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
                //{id=529528, name=3105613C, type=bp, uuid=3105613C, image=, title=Bell button pressed on 3105613C, value=0, comment=, created={"date":"2021-09-13 10:00:27.976429","timezone":"UTC","timezone_type":3}}
                JSONObject jsonMessage = new JSONObject(remoteMessage.getData());
                try {
                    Log.d(TAG, "id: " + jsonMessage.getString("id"));
                    sendNotification(Integer.parseInt(jsonMessage.getString("id")), jsonMessage.getString("uuid"), jsonMessage.getString("created"),
                            jsonMessage.getString("title") + " at " + jsonMessage.getString("created"), jsonMessage.getString("image"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }

        }


        @Override
        public void onNewToken(String token) {
            Log.d(TAG, "Refreshed token: " + token);
            Context ctx = getApplicationContext();
            this.registerToken(token, ctx);
        }

        public static void registerToken(String token, Context ctx){
            //username, phoneid, token, system, system_type, language, country
            String android_id = Settings.Secure.getString(ctx.getContentResolver(),Settings.Secure.ANDROID_ID);
            Log.d("Android","Android ID : "+android_id);
            String locale = ctx.getResources().getConfiguration().getLocales().get(0).getLanguage();
            Log.d("Android","Android Locale : "+locale);
            String country = ctx.getResources().getConfiguration().getLocales().get(0).getCountry();
            Log.d("Android","Android Country : "+locale);

            RequestQueue queue = Volley.newRequestQueue(ctx);
            String url ="https://ping.ibeyonde.com/api/iot.php?view=token&token=" + token + "&username=" + LoginViewModel._email
                    + "&phone_id=" + android_id + "&system=android&system_type=android&language=" + locale + "&country=" + country;

            StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "sendRegistrationToServer " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "ERROR Response: sendRegistrationToServer failed ," + error.getMessage());
                }
            }){

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> params = new HashMap<String, String>();
                    String creds = String.format("%s:%s", LoginViewModel._email,LoginViewModel._pass);
                    String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                    params.put("Authorization", auth);
                    return params;
                }

            };
            queue.add(stringRequest);
        }

        private void sendNotification(int messageId, String uuid, String datetime, String messageBody, String url) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("uuid", uuid);
            intent.putExtra("datetime", datetime);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, messageId, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Bitmap myBitmap = null;
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setConnectTimeout(1000);
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ico128);
                Log.w(TAG, "Failed do download the alert image");
            }

            NotificationCompat.Builder notificationBuilder;
            if (myBitmap != null ) {
                 notificationBuilder =
                        new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_dashboard_black_24dp)
                                .setContentTitle(getString(R.string.fcm_message))
                                .setContentText(messageBody)
                                .setLargeIcon(myBitmap)
                                .setStyle(new NotificationCompat.BigPictureStyle()
                                        .bigPicture(myBitmap)
                                        .bigLargeIcon(null))
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);
            }
            else {
                notificationBuilder =
                        new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_dashboard_black_24dp)
                                .setContentTitle(getString(R.string.fcm_message))
                                .setContentText(messageBody)
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "CleverCam Bell press alert",
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(messageId, notificationBuilder.build());
        }
    }