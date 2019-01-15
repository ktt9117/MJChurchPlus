package org.mukdongjeil.mjchurch.data.network;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.util.InjectorUtils;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class SermonSyncIntentService extends IntentService {
    private static final String TAG = SermonSyncIntentService.class.getSimpleName();

    private static final String NOTI_CHANNEL_ID = "MJChurchPlus";
    private static final String NOTI_CHANNEL_NAME = "SyncService";

    public static final String INTENT_KEY_FETCH_TYPE = "fetchType";
    public static final String INTENT_VALUE_FETCH_TYPE_SERMON = "sermon";
    public static final String INTENT_VALUE_FETCH_TYPE_SERMON_REPLY = "sermonReply";

    public static final String INTENT_KEY_BBS_NO = "bbsNo";

    public SermonSyncIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String channelId = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(NOTI_CHANNEL_ID, NOTI_CHANNEL_NAME);
            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this, channelId);
            Notification notification = notiBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(1, notification);

        } else {
            startForeground(2, new Notification());
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String fetchType = intent.getStringExtra(INTENT_KEY_FETCH_TYPE);

        if (TextUtils.isEmpty(fetchType)) {
            Crashlytics.log(Log.WARN, TAG, "onHandleIntent do nothing. caused by fetchType is empty");
            return;
        }

        if (fetchType.equals(INTENT_VALUE_FETCH_TYPE_SERMON)) {
            SermonNetworkDataSource networkDataSource =
                    InjectorUtils.provideSermonNetworkDataSource(this.getApplicationContext());
            networkDataSource.fetch();

        } else if (fetchType.equals(INTENT_VALUE_FETCH_TYPE_SERMON_REPLY)) {
            int bbsNo = intent.getIntExtra(INTENT_KEY_BBS_NO, -1);
            if (bbsNo == -1) {
                Crashlytics.log(Log.WARN, TAG, "could not fetch sermon reply. caused by intent value bbsNo is empty");
                return;
            }

            SermonReplyNetworkDataSource networkDataSource =
                    InjectorUtils.provideSermonReplyNetworkDataSource(this.getApplicationContext());
            networkDataSource.fetch(bbsNo);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notiManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notiManger.createNotificationChannel(channel);
        return channelId;
    }
}