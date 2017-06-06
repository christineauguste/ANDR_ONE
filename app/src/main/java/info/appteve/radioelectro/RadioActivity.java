package info.appteve.radioelectro;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import org.json.JSONException;
import org.json.JSONObject;
import co.mobiwise.library.radio.RadioListener;
import co.mobiwise.library.radio.RadioManager;
import de.hdodenhof.circleimageview.CircleImageView;
import info.appteve.radioelectro.constants.Constants;



public class RadioActivity extends AppCompatActivity implements RadioListener,Constants, NavigationView.OnNavigationItemSelectedListener {


    RadioManager mRadioManager = RadioManager.with(this);
    public String idradio;
    public String radioName;
    public String radioUrl;
    public String alldata;
    public String alldataNew;
    public String image_cover_radio_file;
    private Activity activity;
    private boolean runningOnOldConnection;
    private String urlToPlay;
    private static int RETRY_AS = 7000;
    private int errorCounting = 0;
    private static int MAX = 2;
    static int audioSessionIDS = 0;
    public JSONObject radioJson;
    private Boolean isInternet;

    Button mButtonControlStart;
    TextView mTextViewControl;
    CircleImageView artistCover;
    ImageView backImage;
    InterstitialAd mInterstitialAd;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = (RadioActivity.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mButtonControlStart = (Button) findViewById(R.id.playBtn);
        mTextViewControl = (TextView) findViewById(R.id.radiotextField);
        artistCover = (CircleImageView) findViewById(R.id.imgCovers);
        backImage = (ImageView) findViewById(R.id.imageBack);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Utilites.isNetworkAvailable(activity, true);
        mRadioManager = RadioManager.with(activity);
        mRadioManager.registerListener(NotificationBuilder.getStaticNotificationUpdater(activity.getBaseContext()));
        if (!mRadioManager.isConnected()) {
            mRadioManager.connect();
            runningOnOldConnection = false;
        } else {
            runningOnOldConnection = true;
        }
        mRadioManager.enableNotification(false);



        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_banner_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });



        if (isOnline()){

            requestNewInterstitial();
            initializeUI();
            updateCoverImages("");
            getRadioApi();
            isInternet = true;
        } else {

            isInternet = false;
        }



    }

    public boolean isOnline() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public  void  getRadioApi (){

        RadioInfoHelper.getRadioForQuery(new RadioInfoHelper.RadioApiCallback() {
            @Override
            public void finished(JSONObject object) {

                radioJson = object;
                idradio = "1";
                try{
                    radioName = radioJson.getString("name");
                    radioUrl = radioJson.getString("radio_url");
                    image_cover_radio_file = radioJson.getString("image_file");

                    setTitle(radioName);
                    mTextViewControl.setText(radioName);

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            urlToPlay = (ParserHttpUrl.getUrl(radioUrl));

                            if (isPlaying()) {
                                if (!mRadioManager.getService().getRadioUrl().equals(urlToPlay)) {
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(activity, getResources().getString(R.string.alert_network), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }

                    });


                } catch (JSONException e){

                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Errwor: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                }


            }
        },activity);



    }


    private boolean isPlaying() {
        return (null != mRadioManager && null != RadioManager.getService() && RadioManager.getService().isPlaying());
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

    private void startPlaying() {


        mRadioManager.startRadio(urlToPlay);
        mRadioManager.updateNotification(
                activity.getResources().getString(R.string.notification_title),
                activity.getResources().getString(R.string.notification_subtitle),
                R.drawable.defimage,
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.defimage)
        );

    }

    public void stopPlaying() {

        mRadioManager.stopRadio();

        if (runningOnOldConnection) {
            resetRadioManager();
        }
    }

    public void updateMediaInfoFromBackground(String info) {

       alldata = info;

        if (info != null) {
            mTextViewControl.setText(info);
        }
        if (info != null && mTextViewControl.getVisibility() == View.GONE) {
            mTextViewControl.setVisibility(View.VISIBLE);
            mTextViewControl.setVisibility(View.VISIBLE);
        } else if (info == null) {
            mTextViewControl.setVisibility(View.GONE);
            mTextViewControl.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRadioLoading() {

    }

    @Override
    public void onRadioConnected() {
    }

    @Override
    public void onRadioStarted() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onRadioStopped() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {


                    RadioManager.getService().cancelNotification();
            }
        });
    }



    @Override
    public void onMetaDataReceived(String key, final String value) {
        if (key != null && (key.equals("StreamTitle") || key.equals("title")) && !value.equals("")) {

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMediaInfoFromBackground(value);
                }
            });

            updateCoverImages(value);

        }
    }

    @Override
    public void onAudioSessionId(int i) {
        audioSessionIDS = i;
    }

    @Override
    public void onError() {
        Log.d("INFO", "onError");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorCounting < MAX) {


                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            errorCounting += 1;
                            startPlaying();
                        }
                    }, RETRY_AS);
                } else {
                    Toast.makeText(activity, activity.getResources().getString(R.string.error_retry), Toast.LENGTH_SHORT).show();
                    Log.v("INFO", "Received various errors, tried to create a new RadioManager");

                    resetRadioManager();


                } //
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mRadioManager.registerListener(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        mRadioManager.unregisterListener(this);
    }



    private void resetRadioManager() {
        try {
            mRadioManager.disconnect();
        } catch (Exception e) {
        }
        RadioManager.flush();
        mRadioManager = RadioManager.with(activity);
        mRadioManager.connect();
        mRadioManager.registerListener(this);
        mRadioManager.registerListener(NotificationBuilder.getStaticNotificationUpdater(activity.getBaseContext()));
        runningOnOldConnection = false;
    }

    private void updateCoverImages(String infoString) {

        alldataNew = infoString;
            ImageCoverHelper.getImageArtist(infoString, new ImageCoverHelper.AlbumCallback() {
                @Override
                public void finished(Bitmap art) {
                    if (art != null) {
                        artistCover.setImageBitmap(art);
                        Bitmap blurred = blurRenderScript(art, 25);
                        backImage.setImageBitmap(blurred);

                    }
                }
            }, activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.nav_camera) {

            if(isInternet){

                Intent searchIntent = new Intent(RadioActivity.this, RadioActivity.class);
                startActivity(searchIntent);

                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }






        }   else if (id == R.id.nav_manage) {

            if(isInternet){

                Intent searchIntent = new Intent(RadioActivity.this, NewsActivity.class);
                startActivity(searchIntent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }



        } else if (id == R.id.nav_share) {

          if(isInternet){

              Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MENU_SITE_URL));
              startActivity(browserIntent);
          }



        } else if (id == R.id.nav_send) {

          if(isInternet){

              Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MENU_APPLICATIONS_URL));
              startActivity(browserIntent);

          }



        } else if(id == R.id.nav_podcast){

          if (isInternet){

              Intent searchIntent = new Intent(RadioActivity.this, PodcastActivity.class);
              startActivity(searchIntent);
              overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

          }




        } else if(id == R.id.nav_podcast_offline){



            Intent searchIntent = new Intent(RadioActivity.this, OfflinePodcastActivity.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);


        } else if(id == R.id.nav_social){

          if (isInternet){

              Intent searchIntent = new Intent(RadioActivity.this, SocialActivity.class);
              startActivity(searchIntent);
              overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

          }




        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void initializeUI() {


        mButtonControlStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying()) {

                    startPlaying();
                    mButtonControlStart.setBackgroundResource(R.drawable.pause_button_a);
                    mTextViewControl.setText(alldataNew);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            showInterstitial();
                        }
                    }, 5000);

                }else {

                    stopPlaying();
                    mButtonControlStart.setBackgroundResource(R.drawable.play_button_a);

                }
            }
        });

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

            Log.d("DDD","Tape menu");



        } else {
            super.onBackPressed();
            Log.d("DDD","Tape back");

        }



    //    stopPlaying();
        mButtonControlStart.setBackgroundResource(R.drawable.play_button_a);
    }



    public void chat_Click (View view) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "I listen : "+alldataNew+" in" + APP_NAME + "application. Download app " + GOOGLE_APP_URL);
        startActivity(Intent.createChooser(intent, "Share with"));

    }

    /// blur

    @SuppressLint("NewApi")
    private Bitmap blurRenderScript(Bitmap smallBitmap, int radius) {

        try {
            smallBitmap = RGB565toARGB888(smallBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Bitmap bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);



        RenderScript renderScript = RenderScript.create(RadioActivity.this);

        Allocation blurInput = Allocation.createFromBitmap(renderScript, smallBitmap);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius);
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;

    }

    private Bitmap RGB565toARGB888(Bitmap img) throws Exception {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }



}


