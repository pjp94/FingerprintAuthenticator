package com.pancholi.fingerprintauthenticator;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

/***
 * This class implements the actual starting and stopping of the fingerprint listener
 * and also handles the Authentication callbacks.
 */
public class FingerprintAuthenticator
        extends FingerprintManager.AuthenticationCallback {

  private Callback callback;
  private CancellationSignal signal;
  private boolean canceled;

  private FingerprintManager fingerprintManager;

  FingerprintAuthenticator(Callback callback,
                           FingerprintManager fingerprintManager) {
    this.callback = callback;
    this.fingerprintManager = fingerprintManager;
  }

  void listenForFingerprint(FingerprintManager.CryptoObject cryptoObject) {
    signal = new CancellationSignal();
    canceled = false;
    fingerprintManager.authenticate(cryptoObject, signal, 0, this, null);
  }

  void cancelListener() {
    if (signal != null) {
      signal.cancel();
      signal = null;
      canceled = true;
    }
  }

  @Override
  public void onAuthenticationError(int errMsg, CharSequence errString) {
    // If the listener wasn't canceled yet some error occurred while authenticating
    // the fingerprint, display an error.
    if (!canceled) {
      callback.onError();
    }
  }

  @Override
  public void onAuthenticationFailed() {
    callback.onFail();
  }

  @Override
  public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
    callback.onAuthenticated();
  }

  public interface Callback {
    void onAuthenticated();
    void onFail();
    void onError();
  }
}
