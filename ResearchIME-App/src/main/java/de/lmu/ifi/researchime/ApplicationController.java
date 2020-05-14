package de.lmu.ifi.researchime;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.orm.SugarApp;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import de.lmu.ifi.researchime.contentabstraction.RIMEInputContentProcessingController;
import de.lmu.ifi.researchime.contentextraction.DbInitializer;

public class ApplicationController extends SugarApp {

    public static final String NOTIFICATION_CHANNEL_ID = "RESEARCHIME_NOTIFICATION_CHANNEL";

    @Override
    public void onCreate() {
        super.onCreate();

        FlowManager.init(new FlowConfig.Builder(this).build());
        RIMEInputContentProcessingController.initDatabase(getApplicationContext());
        DbInitializer.initRimeContentExtractionDb(getApplicationContext());

        createNotificationChannel(getApplicationContext());

        Thread.setDefaultUncaughtExceptionHandler(new AppExceptionHandler());
    }

    private void createNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library

            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.app_name);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(channel);
        }
    }

}
