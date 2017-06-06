package info.appteve.radioelectro;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import info.appteve.radioelectro.constants.Constants;

public class OfflinePlayerActivity extends AppCompatActivity  implements Constants{


    final ArrayList<OfflinePodcastItem> offpodcastItems = new ArrayList<>();
    private ProgressDialog pDialog;
    InterstitialAd mInterstitialAd;

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    Context context;
    ProgressBar progressBar;
    ProgressDialog dialogz;
    String files;
    String names;
    String urls;
    Uri uurls;

    int position = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_player);

        RadioActivity radio = new RadioActivity();
        radio.stopPlaying();

        position = getIntent().getExtras().getInt("position");
        generateLisPodcast();

        files = getIntent().getExtras().getString("track_file");
        names  = getIntent().getExtras().getString("track_name");

        createPlayer(files,names);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    mediaPlayer.pause();
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {

                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_banner_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();



    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    void showInterstitial(){

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
        }


    }

    private  void  createPlayer(String url, String trackname){

        urls = Environment.getExternalStorageDirectory()+ SDCARD_FOLDER + trackname;

        mediaPlayer = new MediaPlayer();

        try {

            mediaPlayer.setDataSource(urls);
            mediaPlayer.prepare();
            final ProgressDialog dialog = new ProgressDialog(OfflinePlayerActivity.this);
            dialog.setMessage("Prepare to play. Please wait.");
            dialog.setCancelable(false);
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.show();

            String str_track = trackname.replaceFirst("\\.mp3$", "");

            ((TextView)findViewById(R.id.now_playing_text)).setText(str_track);

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(final MediaPlayer mp) {

                    mp.start();

                    seekBar = (SeekBar) findViewById(R.id.seekBar);

                    mRunnable.run();

                    dialog.dismiss();
                    showInterstitial();

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            continiousPlay();
                        }
                    });
                }
            });

        } catch (IOException e) {
            Activity a = this;
            a.finish();
            Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
        }



    }


    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            if(mediaPlayer != null) {

                int mDuration = mediaPlayer.getDuration();
                seekBar.setMax(mDuration);

                TextView totalTime = (TextView) findViewById(R.id.totalTime);
                totalTime.setText(getTimeString(mDuration));

                int mCurrentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(mCurrentPosition);

                TextView currentTime = (TextView) findViewById(R.id.currentTime);
                currentTime.setText(getTimeString(mCurrentPosition));

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(mediaPlayer != null && fromUser){
                            mediaPlayer.seekTo(progress);
                        }
                    }
                });


            }

            mHandler.postDelayed(this, 10);
        }
    };



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }



    public void playOff(View view){

        mediaPlayer.start();
    }


    public void pauseOff(View view){

        mediaPlayer.pause();

    }

    public void stop(View view){

        mediaPlayer.seekTo(0);
        mediaPlayer.pause();

    }



    public void onBackPressed(){
        super.onBackPressed();

        Intent intent = new Intent(OfflinePlayerActivity.this, OfflinePodcastActivity.class);
        startActivity(intent);


        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        finish();
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        long hours = millis / (1000*60*60);
        long minutes = ( millis % (1000*60*60) ) / (1000*60);
        long seconds = ( ( millis % (1000*60*60) ) % (1000*60) ) / 1000;

        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }



    public void generateLisPodcast(){


        File rirr = new File(Environment.getExternalStorageDirectory() +
                File.separator + SDCARD_FOLDER_W +"/" );

        offpodcastItems.clear();

        for (File f : rirr.listFiles()) {
            if (f.isFile())
            {
                String namez = f.getName();
                Log.d("filez", namez);
                String filedir = rirr + File.separator+ namez;
                Log.d("filez", filedir);

                offpodcastItems.add(new OfflinePodcastItem(namez,filedir));

            }



        }
    }

    public void nextTrackOff(View view){


        if (position == offpodcastItems.size() - 1){

            position = 0;
        } else {

            position = position + 1;
        }

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }



        names = offpodcastItems.get(position).offtrack_name;
        files = offpodcastItems.get(position).offtrack_file;

        createPlayer(files,names);
        showInterstitial();


    }

    public void forwardOff (View view){



        if (position == 0){

            position = offpodcastItems.size() - 1;
        } else {

            position = position - 1;
        }



        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }



        names = offpodcastItems.get(position).offtrack_name;
        files = offpodcastItems.get(position).offtrack_file;

        createPlayer(files,names);

    }

    public void continiousPlay(){

        if (position == offpodcastItems.size() - 1){

            position = 0;
        } else {

            position = position + 1;
        }

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }



        names = offpodcastItems.get(position).offtrack_name;
        files = offpodcastItems.get(position).offtrack_file;

        createPlayer(files,names);
    }


}
