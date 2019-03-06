package com.pancholi.fingerprintauthenticator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.view.LayoutInflater;
import android.widget.Toast;

import java.util.Random;

/***
 * Custom dialog class to present the login dialog that prompts the user to scan their
 * fingerprint if they're able to. This class also handles starting and stopping the
 * listener that listens for the fingerprint scan.
 */
public class FingerprintDialog extends AlertDialog
        implements FingerprintAuthenticator.Callback {

  private static final String USER_ID = "user_id";

  private FingerprintAuthenticator authenticator;
  private Context context;

  FingerprintDialog(Context context, FingerprintManager.CryptoObject cryptoObject) {
    super(context);
    this.context = context;

     authenticator = new FingerprintAuthenticator(this,
             context.getSystemService(FingerprintManager.class));

    // Begin listening for the fingerprint when the dialog is created.
    authenticator.listenForFingerprint(cryptoObject);
    createDialog();
  }

  @SuppressLint("InflateParams")
  private void createDialog() {
    LayoutInflater inflater = LayoutInflater.from(context);

    setView(inflater.inflate(R.layout.fingerprint_dialog, null));
    setButton(BUTTON_POSITIVE, context.getString(R.string.cancel), (dialogInterface, i) -> { });

    // Stop listening for a fingerprint if the dialog is closed in any way.
    setOnDismissListener((dialog) -> cancelListener());
  }

  private void cancelListener() {
    authenticator.cancelListener();
  }

  @Override
  public void onAuthenticated() {
    Random random = new Random();

    // Create a random user ID between 1-10000 to mimic some user logging in.
    int userId = random.nextInt(10000) + 1;

    Intent intent = new Intent(context, WelcomeActivity.class);
    intent.putExtra(USER_ID, userId);

    context.startActivity(intent);
    ((LoginActivity) context).finish();
  }

  @Override
  public void onFail() {
    Toast.makeText(context, context.getString(R.string.toast_fail), Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onError() {
    Toast.makeText(context, context.getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
  }
}
