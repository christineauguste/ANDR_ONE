package info.appteve.radioelectro;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.onesignal.OneSignal;


public class AppController extends Application {
    public static final String TAG = AppController.class
            .getSimpleName();

    SharedPreferences prefs = null;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();


        OneSignal.startInit(this).init();

        FontsOverride.setDefaultFont(this, "DEFAULT", "appteve/Oswaldesque-Regular.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "appteve/Oswaldesque-Regular.ttf");
        FontsOverride.setDefaultFont(this, "SERIF", "appteve/Oswaldesque-Regular.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "appteve/Oswaldesque-Regular.ttf");



        prefs = getSharedPreferences("info.com.castio", MODE_PRIVATE);

        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putBoolean("firstrun", false).commit(); //prefs

        }

    }


}
