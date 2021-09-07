package com.ibeyonde.cam.utils;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.R;
import com.ibeyonde.cam.ui.device.history.HistoryViewModel;
import com.ibeyonde.cam.ui.login.LoginViewModel;


import java.util.HashMap;
import java.util.Map;

public class CCFirebaseMessagingService  extends FirebaseMessagingService {
    private static final String TAG= CCFirebaseMessagingService.class.getCanonicalName();

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            Log.d(TAG, "From: " + remoteMessage.getFrom());

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());

                // Handle message within 10 seconds
                //handleNow();
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
            //username, phoneid, token, system, system_type, language, country
            String android_id = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
            Log.d("Android","Android ID : "+android_id);
            String locale = ctx.getResources().getConfiguration().getLocales().get(0).getCountry();
            Log.d("Android","Android Locale : "+locale);
            String country = ctx.getResources().getConfiguration().getLocales().get(0).getDisplayCountry();
            Log.d("Android","Android Country : "+locale);

            RequestQueue queue = Volley.newRequestQueue(ctx);
            String url ="https://ping.ibeyonde.com/api/iot.php?view=token&token=" + token + "&username=" + LoginViewModel._email
                    + "&phoneid=" + android_id + "&system=android&system_type=android&language=";

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

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    120000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
        /**
         * Schedule async work using WorkManager.
         */
        private void scheduleJob() {
            // [START dispatch_job]
            /**OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                    .build();
            WorkManager.getInstance(this).beginWith(work).enqueue();
             **/
            // [END dispatch_job]
        }

        /**
         * Handle time allotted to BroadcastReceivers.
         */
        private void handleNow() {
            Log.d(TAG, "Short lived task is done.");
        }


        /**
         * Create and show a simple notification containing the received FCM message.
         *
         * @param messageBody FCM message body received.
         */
        private void sendNotification(String messageBody) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_dashboard_black_24dp)
                            .setContentTitle(getString(R.string.fcm_message))
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }