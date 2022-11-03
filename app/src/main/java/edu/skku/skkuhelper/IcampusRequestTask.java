package edu.skku.skkuhelper;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IcampusRequestTask extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
        // Fetch data from the API in the background.

        String result = "";
        try {
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL("https://canvas.skku.edu/api/v1/users/self/favorites/courses");
                //open a URL coonnection
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader isw = new InputStreamReader(in);

                int data = isw.read();

                while (data != -1) {
                    result += (char) data;
                    data = isw.read();

                }

                // return the data to onPostExecute method
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
