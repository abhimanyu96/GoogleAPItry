package com.example.gfoogle;


import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.InputStream;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener,ConnectionCallbacks,OnConnectionFailedListener{

	private static final int RC_SIGN_IN = 0;
	private static final String TAG = "ACTIVITY";
	private GoogleApiClient mGoogleApiClient;
	private boolean mIntentInProgress;

	private boolean mSignInClicked;

	private ConnectionResult mConnectionResult;

	private SignInButton btnSignIn;
	private Button btnSignOut, btnRevokeAccess;
	private ImageView imgProfilePic;
	private TextView txtName, txtEmail;
	private LinearLayout llProfileLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnSignIn = (SignInButton)findViewById(R.id.btn_sign_in);
		btnSignOut = (Button)findViewById(R.id.btn_sign_out);
		btnRevokeAccess = (Button)findViewById(R.id.btn_revoke_access);
		imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
		txtName = (TextView) findViewById(R.id.txtName);
		txtEmail = (TextView) findViewById(R.id.txtEmail);
		llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);
		btnRevokeAccess.setOnClickListener(this);
		btnSignOut.setOnClickListener(this);
		btnSignIn.setOnClickListener(this);
		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mGoogleApiClient.connect();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
		if(mGoogleApiClient.isConnected())
		{
			mGoogleApiClient.disconnect();
		}
		super.onStop();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		if(!result.hasResolution())
		{
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
			return;
		}
		if (!mIntentInProgress) {
			// Store the ConnectionResult for later usage
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}

	}
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		// TODO Auto-generated method stub
		if (requestCode == RC_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

		mSignInClicked = false;
		Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

		// Get user's information
		getProfileInformation();

		// Update the UI after signin
		updateUI(true);
	}

	private void getProfileInformation() {
		// TODO Auto-generated method stub
		try{
			if(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient)!=null)
			{
				Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
				String personName = currentPerson.getDisplayName();
				String personPhotoURL = currentPerson.getImage().getUrl();
				String personGooglePlusProfile = currentPerson.getUrl();
				String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
				Log.e(TAG, "Name: " + personName + ", plusProfile: "
						+ personGooglePlusProfile + ", email: " + email
						+ ", Image: " + personPhotoURL);
				//check error
				txtName.setText(personName);
				txtEmail.setText(email);
				//last two char in URL specify size of the image. Removing and adding custom size
				personPhotoURL=personPhotoURL.substring(0, personPhotoURL.length()-2) + 400;
				new loadProfileImage(imgProfilePic).execute(personPhotoURL);
			}
			else
			{
				Toast.makeText(getApplicationContext(),
						"Person information is null", Toast.LENGTH_LONG).show();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private class loadProfileImage extends AsyncTask<String, Void , Bitmap>
	{
		private ImageView bmImage;
		public loadProfileImage(ImageView bmImage) {
			// TODO Auto-generated constructor stub
			this.bmImage=bmImage;
		}
		@Override
		protected Bitmap doInBackground(String... urls) {
			// TODO Auto-generated method stub
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			bmImage.setImageBitmap(result);
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		mGoogleApiClient.connect();
		updateUI(false);
	}

	private void updateUI(boolean isSignedIn) {
		// TODO Auto-generated method stub
		if(isSignedIn)
		{
			btnSignOut.setVisibility(View.VISIBLE);
			btnSignIn.setVisibility(View.GONE);
			btnRevokeAccess.setVisibility(View.VISIBLE);
			llProfileLayout.setVisibility(View.VISIBLE);
		}
		else
		{
			btnSignOut.setVisibility(View.GONE);
			btnSignIn.setVisibility(View.VISIBLE);
			btnRevokeAccess.setVisibility(View.GONE);
			llProfileLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_sign_in:
			// Signin button clicked
			
			signInWithGplus();
			break;
		case R.id.btn_sign_out:
			// Signout button clicked
			signOutFromGplus();
			break;
		case R.id.btn_revoke_access:
			// Revoke access button clicked
			revokeGplusAccess();
			break;
		}
	}

	private void revokeGplusAccess() {
		// TODO Auto-generated method stub

	}

	private void signOutFromGplus() {
		// TODO Auto-generated method stub
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
			updateUI(false);
		}
	}

	private void signInWithGplus() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Button Working", Toast.LENGTH_SHORT).show();
		if(!mGoogleApiClient.isConnecting())
		{

			mSignInClicked=true;
			resolveSignInError();
		}
	}

	private void resolveSignInError() {
		// TODO Auto-generated method stub
		if(mConnectionResult.hasResolution())
		{
			try
			{
				mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
			}
			catch(SendIntentException e)
			{
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}
}
