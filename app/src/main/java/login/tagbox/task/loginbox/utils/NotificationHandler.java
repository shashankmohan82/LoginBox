package login.tagbox.task.loginbox.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import login.tagbox.task.loginbox.R;
import login.tagbox.task.loginbox.activities.LoginActivity;
import static android.content.Context.NOTIFICATION_SERVICE;
/**
 * Created by shashank on 5/27/2017.
 */
public class NotificationHandler {

    private Context context;
    private int mNotificationId = 01;

    public NotificationHandler(Context context){
        this.context = context;
    }

    public void showFailedSigninNotification() {

        Intent resultIntent = new Intent(context.getApplicationContext(), LoginActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_ONE_SHOT
                );

        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo_notification)
                        .setColor(context.getResources().getColor(R.color.colorAccent))
                        .setContentTitle(context.getString(R.string.failed_signin))
                        .setAutoCancel(true)
                        .setContentText(context.getString(R.string.user_credentials_mismatch));

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
