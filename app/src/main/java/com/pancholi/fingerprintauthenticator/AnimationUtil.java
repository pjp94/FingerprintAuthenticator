package com.pancholi.fingerprintauthenticator;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/***
 * Simple utility class to handle animating the Views in the Welcome activity.
 */
class AnimationUtil {

  private static final float ALPHA_START = 0.0f;
  private static final float ALPHA_END = 1.0f;

  static void animate(@NonNull View welcomeMessage, @NonNull final View signOutButton) {
    Animation animation = new AlphaAnimation(ALPHA_START, ALPHA_END);

    animation.setDuration(3000);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) { }

      @Override
      public void onAnimationEnd(Animation animation) {
        signOutButton.setVisibility(View.VISIBLE);
      }

      @Override
      public void onAnimationRepeat(Animation animation) { }
    });

    welcomeMessage.startAnimation(animation);
  }
}
