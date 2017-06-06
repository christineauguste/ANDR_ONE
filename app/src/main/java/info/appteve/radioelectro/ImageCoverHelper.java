package info.appteve.radioelectro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;


import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;


public class ImageCoverHelper {

    final static BitmapFactory.Options options = new BitmapFactory.Options();

    public static String getImageArtist(final String query, final AlbumCallback albumCallback, final Context context) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... unused) {
                String queryURL = "https://itunes.apple.com/search?term="+ URLEncoder.encode(query) +"&limit=1";

                JSONObject jsonObject = Utilites.getJSONObjectFromUrl(queryURL);

                try {
                    if (jsonObject != null  ) {

                        int counting = jsonObject.getInt("resultCount");

                        JSONArray resp = jsonObject.getJSONArray("results");
                        JSONObject obj = resp.getJSONObject(0);
                        String urlImage = obj.getString("artworkUrl100");
                        urlImage = urlImage.replace("100x100bb","300x300bb");

                        return urlImage;
                    } else {
                        Log.v("INFO", "Cover not found");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(final String imageUrl) {

                if (imageUrl != null) {
                    Picasso.with(context)
                            .load(imageUrl)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                    albumCallback.finished(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                    albumCallback.finished(null);
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                }else {


                options.inJustDecodeBounds = true;
                options.inSampleSize = 2;
                options.inJustDecodeBounds = false;
                options.inTempStorage = new byte[16 * 1024];

                Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.defimage);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, 300, 300, false);
                    albumCallback.finished(resizedBitmap);
                }
            }
        }.execute();

        return null;
    }

    public interface AlbumCallback {
        void finished(Bitmap b);
    }
}
