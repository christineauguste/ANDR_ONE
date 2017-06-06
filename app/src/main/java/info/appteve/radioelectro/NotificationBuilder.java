package info.appteve.radioelectro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import co.mobiwise.library.radio.RadioListener;
import co.mobiwise.library.radio.RadioManager;

public class NotificationBuilder implements RadioListener {

    private Context context;
    private static NotificationBuilder notificationBuilder;

    public static NotificationBuilder getStaticNotificationUpdater(Context context) {
        if (notificationBuilder == null) {
            notificationBuilder = new NotificationBuilder(context);
        }

        return notificationBuilder;
    }

    private NotificationBuilder(Context context) {
        this.context = context;

    }

    @Override
    public void onMetaDataReceived(String key, String value) {
        if (key != null && (key.equals("StreamTitle") || key.equals("title")) && !value.equals("")) {
            String title = value;
            String artist = "";
            if (value.contains(" - ")) {
                title = value.split("-")[1];
                artist = value.split("-")[0];
            }

            if (value == null || value.equals("")) value = title;
            final String infoString = value;

            RadioManager.getService().updateNotification(title, artist,
                    R.drawable.defimage,
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.defimage));

            updateAlbumArt(infoString, title, artist);

        }
    }

    private void updateAlbumArt(String infoString, final String title, final String artist) {
        ImageCoverHelper.getImageArtist(infoString, new ImageCoverHelper.AlbumCallback() {
            @Override
            public void finished(Bitmap art) {
                if (art != null) {
                    RadioManager.getService().updateNotification(title, artist, R.drawable.defimage, art);
                }
            }
        }, context);
    }

    @Override
    public void onAudioSessionId(int i) {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onRadioLoading() {

    }

    @Override
    public void onRadioConnected() {

    }

    @Override
    public void onRadioStarted() {

    }

    @Override
    public void onRadioStopped() {
    }
}
