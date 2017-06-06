package info.appteve.radioelectro;

import android.content.Context;

import android.os.AsyncTask;
import android.util.Log;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import info.appteve.radioelectro.constants.Constants;

/**
 * Created by appteve on 11/01/2017.
 */

public class RadioInfoHelper implements Constants{


    static String getRadioForQuery ( final RadioApiCallback apicallback, final Context context){

        new AsyncTask<Void, Void,JSONObject>(){
            @Override
            protected JSONObject doInBackground(Void... unused){

                String url = GENERAL_API_URL + URL_API_GET_LISTRADIO +  KEY_STRING + API_KEY;

                JSONArray objectJson = Utilites.getJSONArrayFromUrl(url);

                try {

                    if (objectJson != null){

                        JSONObject person = (JSONObject) objectJson
                                .get(0);


                        return person;


                    } else {

                        Log.v("INFO", "No items in Radio api Request");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(final JSONObject objects) {

                if (objects != null) {

                    apicallback.finished(objects);

                }else {

                    apicallback.finished(null);
                }
            }
        }.execute();

        return null;

    }


    public interface RadioApiCallback{

        void finished (JSONObject object);

    }

    /// fetch news api

    static String getNewsForQuery (final NewsApiCallback apicallbacknews, final Context context){

        new AsyncTask<Void, Void,JSONArray>(){
            @Override
            protected JSONArray doInBackground(Void... unused){

                String url = GENERAL_API_URL + URL_API_GET_NEWS + API_KEY;

                JSONArray objectJson = Utilites.getJSONArrayFromUrl(url);


                    if (objectJson != null) {

                        JSONArray ars = objectJson;

                        System.out.println(ars.toString());


                        return ars;


                    } else {

                        Log.v("INFO", "No items in Radio api Request");
                    }




                return null;
            }

            @Override
            protected void onPostExecute(final JSONArray objectsApi) {

                if (objectsApi != null) {

                    apicallbacknews.finishedNews(objectsApi);

                }else {



                    apicallbacknews.finishedNews(null);
                }
            }
        }.execute();

        return null;



    }


    public interface NewsApiCallback{

        void finishedNews (JSONArray objectz);

    }

    /// fetch podcast api

    static String getPodcastForQuery (final PodcastApiCallback apicallbackpodcast, final Context context){

        new AsyncTask<Void, Void,JSONArray>(){
            @Override
            protected JSONArray doInBackground(Void... unused){

                String url = GENERAL_API_URL + URL_API_GET_PODCAST + API_KEY;

                JSONArray objectJson = Utilites.getJSONArrayFromUrl(url);

                if (objectJson != null) {

                    JSONArray ars = objectJson;

                    System.out.println(ars.toString());


                    return ars;


                } else {

                    Log.v("INFO", "No items in Radio api Request");
                }




                return null;
            }

            @Override
            protected void onPostExecute(final JSONArray objectsApi) {

                if (objectsApi != null) {

                    apicallbackpodcast.finishedPodcast(objectsApi);

                }else {

                    apicallbackpodcast.finishedPodcast(null);
                }
            }
        }.execute();

        return null;



    }


    public interface PodcastApiCallback{

        void finishedPodcast (JSONArray objectz);

    }






}
