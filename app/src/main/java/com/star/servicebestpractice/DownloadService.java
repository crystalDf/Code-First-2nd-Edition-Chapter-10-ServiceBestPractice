package com.star.servicebestpractice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {

    private static final String NOTIFICATION_CHANNEL_ID = "NotificationChannelId";
    private static final int NOTIFICATION_ID = 0;

    private DownloadTask mDownloadTask;
    private String mDownloadUrl;

    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(NOTIFICATION_ID,
                    getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            mDownloadTask = null;

            stopForeground(true);

            getNotificationManager().notify(NOTIFICATION_ID,
                    getNotification("Download Success", -1));

            Toast.makeText(DownloadService.this, "Download Success",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailed() {
            mDownloadTask = null;

            stopForeground(true);

            getNotificationManager().notify(NOTIFICATION_ID,
                    getNotification("Download Failed", -1));

            Toast.makeText(DownloadService.this, "Download Failed",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPaused() {
            mDownloadTask = null;

            Toast.makeText(DownloadService.this, "Download Paused",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCanceled() {
            mDownloadTask = null;

            stopForeground(true);

            Toast.makeText(DownloadService.this, "Download Canceled",
                    Toast.LENGTH_LONG).show();
        }
    };

    private DownloadBinder mDownloadBinder = new DownloadBinder();

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDownloadBinder;
    }

    class DownloadBinder extends Binder {

        public void startDownload(String url) {

            if (mDownloadTask == null) {
                mDownloadUrl = url;
                mDownloadTask = new DownloadTask(mDownloadListener);
                mDownloadTask.execute(mDownloadUrl);

                startForeground(NOTIFICATION_ID, getNotification("Downloading...", 0));

                Toast.makeText(DownloadService.this, "Downloading...",
                        Toast.LENGTH_LONG).show();
            }
        }

        public void pauseDownload() {

            if (mDownloadTask != null) {
                mDownloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {

            if (mDownloadTask != null) {
                mDownloadTask.cancelDownload();
            } else if (mDownloadUrl != null) {
                String fileName = mDownloadUrl.substring(mDownloadUrl.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory + fileName);

                if (file.exists()) {
                    file.delete();
                }

                getNotificationManager().cancel(NOTIFICATION_ID);

                stopForeground(true);

                Toast.makeText(DownloadService.this, "Canceled",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, NOTIFICATION_CHANNEL_ID);

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setContentTitle(title);

        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }

        return builder.build();
    }
}
