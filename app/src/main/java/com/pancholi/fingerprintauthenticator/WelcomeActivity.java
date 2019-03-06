package com.pancholi.fingerprintauthenticator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;

/***
 * Secondary activity that displays a welcome message if the user has successfully
 * scanned their fingerprint.
 */
public class WelcomeActivity extends BaseActivity {

  private static final String USER_ID = "user_id";
  private static final String IS_DEVICE_ROTATED = "is_device_rotated";

  private boolean isDeviceRotated = false;

  @BindView(R.id.welcome_message) TextView welcomeMessage;
  @BindView(R.id.sign_out_button) Button signOut;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_welcome);

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    int userId = 1;

    if (extras != null) {
      userId = extras.getInt(USER_ID);
    }

    welcomeMessage.setText(String.format(getString(R.string.welcome_message), userId));

    if (savedInstanceState == null
            || !savedInstanceState.getBoolean(IS_DEVICE_ROTATED)) {
      signOut.setVisibility(View.INVISIBLE);
      AnimationUtil.animate(welcomeMessage, signOut);
    }

    isDeviceRotated = true;
  }

  @Override
  protected void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);

    savedInstanceState.putBoolean(IS_DEVICE_ROTATED, isDeviceRotated);
  }

  @Override
  public void onBackPressed() { }

  @OnClick(R.id.sign_out_button)
  void signOut() {
    startActivity(new Intent(this, LoginActivity.class));
    finish();
  }
}
