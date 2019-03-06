package com.pancholi.fingerprintauthenticator;

import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/***
 * This class allows all classes that extend it to implicitly call ButterKnife.bind()
 * when they call setContentView().
 */
public abstract class BaseActivity extends AppCompatActivity {

  @Override
  public void setContentView(@android.support.annotation.LayoutRes int layoutResId) {
    super.setContentView(layoutResId);
    ButterKnife.bind(this);
  }
}
