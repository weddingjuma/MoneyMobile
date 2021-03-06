package com.moneymobile;

import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import beans.User;
import util.ActivityUtil;

public class CoopterUnAmiActivity extends BaseActivity {

	private ProgressDialog pd;
	private TextView emailTextView;
	private Button coopterButton;
	private String emailString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coopter_un_ami);
		emailTextView = (TextView) findViewById(R.id.emailTextView);
		coopterButton = (Button) findViewById(R.id.validerCoopterButton);

		coopterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Test présence internet
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

				if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
					//boolean wifi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
					emailString = emailTextView.getText().toString();
					new coopterTask().execute();
				} else {
					Toast.makeText(CoopterUnAmiActivity.this, "Vous n'etes pas connecté à Internet", Toast.LENGTH_LONG).show();
					ActivityUtil.switchActivity(CoopterUnAmiActivity.this, AccueilActivity.class, new Bundle(), true);
				}
			}
		});
	}



	private class coopterTask extends AsyncTask<String, String, String> {

		protected void onPreExecute() {
			super.onPreExecute();

			pd = new ProgressDialog(CoopterUnAmiActivity.this);
			pd.setMessage("Veuillez patienter");
			pd.setCancelable(false);
			pd.show();
		}

		protected String doInBackground(String... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://10.0.2.2/moneymobile/coopter.php");

			// Request parameters and other properties.
			List<NameValuePair> parameters = new ArrayList<NameValuePair>(2);
			parameters.add(new BasicNameValuePair("telephone", User.getTelephone()));
			parameters.add(new BasicNameValuePair("password", User.getMdp()));
			parameters.add(new BasicNameValuePair("email", emailString));
			try {
				httppost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			}
			catch (UnsupportedEncodingException e) {e.printStackTrace();}

			//Execute and get the response
			try {
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();

				if (entity != null) {

					InputStream instream = entity.getContent();
					StringBuilder buffer = new StringBuilder();
					String line;
					BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));

					line = reader.readLine();
					buffer.append(line);
					while ((line = reader.readLine()) != null) {
						buffer.append("\n" + line);
					}
					return buffer.toString();
				}
			}
			catch (IOException e) {e.printStackTrace();}
			return "erreurFin";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (pd.isShowing()) {
				pd.dismiss();
			}
			System.out.println(result);
			Toast.makeText(CoopterUnAmiActivity.this, result, Toast.LENGTH_LONG).show();
			ActivityUtil.switchActivity(CoopterUnAmiActivity.this, AccueilActivity.class, new Bundle(), true);
		}
	}
}
