package com.pancholi.fingerprintauthenticator;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

/***
 * Simple utility class to retrieve the drawable from the View passed into gettDrawable().
 */
class ViewUtil {

  private static final int RIGHT_COMPOUND_DRAWABLE_INDEX = 2;

  static Drawable getDrawable(@NonNull View view) {
    return ((EditText) view).getCompoundDrawables()[RIGHT_COMPOUND_DRAWABLE_INDEX];
  }
}
