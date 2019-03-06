package com.pancholi.fingerprintauthenticator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import butterknife.BindView;
import butterknife.OnTextChanged;
import butterknife.OnTouch;

/***
 * Entry point for the app. In here, the user is prompted to scan their fingerprint,
 * and the authentication process is begun. The Cipher, KeyStore, and KeyGenerator
 * are all initialized here.
 */
public class LoginActivity extends BaseActivity {

  private static final String IS_DIALOG_SHOWING = "is_dialog_showing";
  private static final String FINGERPRINT_ALIAS = "fingerprint_alias";
  private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

  private FingerprintDialog dialog;

  private Cipher cipher;
  private KeyStore keyStore;
  private KeyGenerator keyGenerator;

  private boolean isDialogShowing = false;
  private boolean isScannerAndFingerprintPresent = true;

  @BindView(R.id.email_input) EditText emailInput;
  @BindView(R.id.password_input) EditText passwordInput;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    setAuthentication();

    // The dialog that prompts the user to scan their fingerprint will only show
    // if the device is able to support fingerprint scanning, or if it was already
    // showing on the previous screen orientation.
    if ((savedInstanceState == null
            || savedInstanceState.getBoolean(IS_DIALOG_SHOWING))
            && isScannerAndFingerprintPresent) {
      showFingerprintDialog();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);

    savedInstanceState.putBoolean(IS_DIALOG_SHOWING, isDialogShowing);
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (dialog != null) {
      isDialogShowing = dialog.isShowing();

      if (isDialogShowing) {
        dialog.dismiss();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    setAuthentication();
  }

  private void setAuthentication() {
    // Only initialize the authentication-related objects if the device
    // can scan and authenticate fingerprints.
    if (verifyDevice()) {
      generateKeyStore();
      createKey();
      createCipher();
    }
  }

  /***
   * Checks if the device has fingerprint scanning hardware and if there are any
   * fingerprints currently saved on the device.
   *
   * @return True if the device has fingerprint scanning hardware and fingerprints
   *         saved; false otherwise.
   */
  private boolean verifyDevice() {
    FingerprintManager fingerprintManager =
            (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

    if (fingerprintManager == null) {
      return false;
    }

    boolean isDeviceReady = false;

    if (!fingerprintManager.isHardwareDetected()
            || !fingerprintManager.hasEnrolledFingerprints()) {
      isScannerAndFingerprintPresent = false;
    } else {
      isDeviceReady = true;
      isScannerAndFingerprintPresent = true;
    }

    return isDeviceReady;
  }

  /***
   * Gets instances of the KeyStore and KeyGenerator. The KeyGenerator is initialized
   * with an AES algorithm, which matches what the Cipher is initialized with.
   */
  private void generateKeyStore() {
    try {
      keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
    } catch (KeyStoreException e) {
      e.printStackTrace();
    }

    try {
      keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      e.printStackTrace();
    }
  }

  /***
   * Gets an instance of the Cipher with the same algorithm as the KeyStore, along with
   * the block mode and padding that matches the key.
   */
  private void createCipher() {
    try {
      cipher = Cipher.getInstance(String.format("%s/%s/%s", KeyProperties.KEY_ALGORITHM_AES,
              KeyProperties.BLOCK_MODE_CBC, KeyProperties.ENCRYPTION_PADDING_PKCS7));
    } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  /***
   * Creates a secret key using KeyGenerator. The key is specified to only be used for
   * encryption or decryption, and it requires user authentication. The block modes and
   * padding are set to be the same as the Cipher.
   */
  private void createKey() {
    try {
      keyStore.load(null);

      KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
              LoginActivity.FINGERPRINT_ALIAS,
              KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
              .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
              .setUserAuthenticationRequired(true)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

      keyGenerator.init(builder.build());
      keyGenerator.generateKey();
    } catch (CertificateException | NoSuchAlgorithmException |
            IOException | InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    }
  }

  /**
   * Initializes the Cipher by retrieving the key specified by FINGERPRINT_ALIAS from
   * the KeyStore. The Cipher is initialized with that key and is specified to only be
   * used for encryption.
   *
   * @param cipher The Cipher that is to be initialized.
   * @return True if the Cipher was successfully initialized using the key retrieved from
   *         the KeyStore; false otherwise.
   */
  private boolean initializeCipher(Cipher cipher) {
    try {
      keyStore.load(null);

      SecretKey key = (SecretKey) keyStore.getKey(LoginActivity.FINGERPRINT_ALIAS, null);
      cipher.init(Cipher.ENCRYPT_MODE, key);

      return true;
    } catch (KeyPermanentlyInvalidatedException e) {
      return false;
    } catch (CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException |
            KeyStoreException | InvalidKeyException | IOException e) {
      throw new RuntimeException("Error creating cipher.", e);
    }
  }

  /***
   * Retrieves the compound drawable in the password input View and either shows
   * the dialog prompting the user to scan their fingerprint or an error dialog
   * if the device is unable to scan and authenticate fingerprints.
   *
   * @param event The motion event that occurred on touch. If the event is not an
   *              up action or the touch did not occur within the bounds of the
   *              drawable, the event will not count as pressing on the drawable.
   * @return True if the drawable was pressed via an up touch action; false otherwise.
   */
  @OnTouch(R.id.password_input)
  protected boolean fingerprintIconClicked(MotionEvent event) {
    Drawable fingerprintIcon = ViewUtil.getDrawable(passwordInput);

    if (fingerprintIcon == null
            || event.getAction() != MotionEvent.ACTION_UP) {
      return false;
    }

    if (event.getRawX() >=
            passwordInput.getRight() -
            fingerprintIcon.getBounds().width() -
            passwordInput.getPaddingRight() -
            passwordInput.getCompoundDrawablePadding()) {
      if (!isScannerAndFingerprintPresent) {
        showErrorDialog();
      } else {
        showFingerprintDialog();

        return true;
      }
    }

    return false;
  }

  @OnTextChanged(R.id.password_input)
  void passwordChanged() {
    setSignInButton(emailInput.getText().length() > 0 && passwordInput.getText().length() > 0);
  }

  @OnTextChanged(R.id.email_input)
  void emailChanged() {
    setSignInButton(emailInput.getText().length() > 0 && passwordInput.getText().length() > 0);
  }

  /***
   * Sets the button color and enabled status depending on if the email and password
   * fields are filled out. Pressing the sign in button doesn't actually do anything;
   * it's just for show.
   *
   * @param isEnabled Determines whether or not the button should be in an enabled
   *                  or disabled state.
   */
  private void setSignInButton(boolean isEnabled) {
    Button signInButton = findViewById(R.id.sign_in_button);

    int textColor;
    int backgroundColor;

    if (isEnabled) {
      textColor = getColor(R.color.colorButtonTextEnabled);
      backgroundColor = R.color.colorPrimary;
    } else {
      textColor = getColor(R.color.colorButtonTextDisabled);
      backgroundColor = R.color.colorButtonBackgroundDisabled;
    }

    signInButton.setTextColor(textColor);
    signInButton.setBackgroundTintList(getColorStateList(backgroundColor));
    signInButton.setEnabled(isEnabled);
  }

  /***
   * Initializes the FingerprintDialog that prompts the user to scan their fingerprint.
   * This starts the fingerprint listener which will attempt to authenticate a fingerprint
   * if one is scanned.
   */
  private void showFingerprintDialog() {
    if (initializeCipher(cipher)) {
      dialog = new FingerprintDialog(this, new FingerprintManager.CryptoObject(cipher));
      dialog.show();
      isDialogShowing = true;
    }
  }

  /***
   * Displays a dialog alerting the user that the app cannot currently scan fingerprints
   * because either the device does not have the necessary hardware or there are no
   * fingerprints saved on the device. The user can go directly to the Security & Location
   * activity in Settings from this dialog and add fingerprints if their phone supports it.
   */
  private void showErrorDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setTitle(R.string.error_title)
            .setMessage(R.string.fingerprint_error)
            .setNegativeButton(R.string.settings_button,
                    (dialogInterface, i) ->
                            startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS)))
            .setPositiveButton(R.string.cancel, (dialogInterface, i) -> { });

    builder.create().show();
  }
}

