package info.appteve.radioelectro;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.Manifest;

import info.appteve.radioelectro.constants.Constants;

public class PlayerCastActivity extends AppCompatActivity implements Constants {


    final ArrayList<PodcastItem> podcastItems = new ArrayList<>();
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;



    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    Context context;
    ProgressBar progressBar;
    ProgressDialog dialogz;
    String files;
    String names;
    String urls;
    int mDuration;
    InterstitialAd mInterstitialAd;

    int position = 0;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_cast);

        ActivityCompat.requestPermissions(PlayerCastActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        RadioActivity radio = new RadioActivity();
        radio.stopPlaying();

        position = getIntent().getExtras().getInt("position");

        getPodcastList();

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + SDCARD_FOLDER_W);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            // Do something on success
        } else {
            // Do something else on failure
        }

        names  = getIntent().getExtras().getString("track_name");
        String mediaUrl = GENERAL_API_URL + MUSIC_FOLDER + getIntent().getExtras().getString("file");
        files = mediaUrl;

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

        urls = url.replaceAll(" ", "%20");
        mediaPlayer = new MediaPlayer();

        try {

            mediaPlayer.setDataSource(urls);
            mediaPlayer.prepareAsync();

            final ProgressDialog dialog = new ProgressDialog(PlayerCastActivity.this);

            dialog.setMessage("Prepare to play. Please wait.");

            dialog.setCancelable(false);

            dialog.getWindow().setGravity(Gravity.CENTER);

            dialog.show();

            ((TextView)findViewById(R.id.now_playing_text)).setText(trackname);



            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(final MediaPlayer mp) {

                    mp.start();

                    seekBar = (SeekBar) findViewById(R.id.seekBar);

                    mRunnable.run();

                    dialog.dismiss();
                    showInterstitial();

                    mDuration = mediaPlayer.getDuration();

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



    public void play(View view){

        mediaPlayer.start();
    }


    public void pause(View view){

        mediaPlayer.pause();

    }

    public void onBackPressed(){
        super.onBackPressed();

        Intent intent = new Intent(PlayerCastActivity.this, PodcastActivity.class);
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

    public void downloadMix(View view){



        dialogz = new ProgressDialog(PlayerCastActivity.this);
        dialogz.setMessage("Track: " +names+" ,downloaded....");
        dialogz.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // dialogz.setIndeterminate(true);
        dialogz.show();

        Ion.with(PlayerCastActivity.this)
                .load(urls)
                .progressBar(progressBar)
                .progressDialog(dialogz)
                .progress(new ProgressCallback() {

                    @Override
                    public void onProgress(long downloaded, long total) {


                    }

                })
                .write(new File(Environment.getExternalStorageDirectory() +
                        File.separator + "Castio/" + names + ".mp3"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File file) {

                        dialogz.dismiss();
                    }
                });



    }

    public void getPodcastList(){

        RadioInfoHelper.getPodcastForQuery(new RadioInfoHelper.PodcastApiCallback() {
            @Override
            public void finishedPodcast(JSONArray objectz) {

                try {

                    for (int i = 0; i < objectz.length(); i++) {

                        JSONObject person = (JSONObject) objectz
                                .get(i);

                        String track_name = person.getString("track_name");
                        String track_file = person.getString("track_file");
                        String file = person.getString("file");

                        podcastItems.add(new PodcastItem(track_name,track_file,file));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }



            }
        }, PlayerCastActivity.this);


    }



    public void nextTrack(View view){


        if (position == podcastItems.size() - 1){

            position = 0;
        } else {

            position = position + 1;
        }

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }



        names = podcastItems.get(position).track_name;
        files = podcastItems.get(position).track_file;

        createPlayer(files,names);


    }

    public void forward (View view){



        if (position == 0){

            position = podcastItems.size() - 1;
        } else {

            position = position - 1;
        }



        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }



        names = podcastItems.get(position).track_name;
        files = podcastItems.get(position).track_file;

        createPlayer(files,names);

    }

    public void continiousPlay(){

        if (position == podcastItems.size() - 1){

            position = 0;
        } else {

            position = position + 1;
        }

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }



        names = podcastItems.get(position).track_name;
        files = podcastItems.get(position).track_file;

        createPlayer(files,names);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    if (ActivityCompat.shouldShowRequestPermissionRationale(PlayerCastActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerCastActivity.this);
                        builder.setTitle("Need Storage Permission");
                        builder.setMessage("This app needs storage permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();


                                ActivityCompat.requestPermissions(PlayerCastActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);


                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



}
