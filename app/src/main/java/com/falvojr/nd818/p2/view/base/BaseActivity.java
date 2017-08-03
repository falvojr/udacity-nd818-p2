package com.falvojr.nd818.p2.view.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.falvojr.nd818.p2.R;
import com.falvojr.nd818.p2.data.prefs.TMDbPreferences;

import java.util.Locale;

/**
 * Base activity with common features.
 * <p>
 * Created by falvojr on 6/4/17.
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private String mApiKey;

    public String getApiKey() {
        return mApiKey;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Stored on secrets.xml resource
        mApiKey = getString(R.string.tmdb_api_key);
    }

    public void showError(int msgIdRes, Throwable error) {
        final String businessMessage = super.getString(msgIdRes);
        Log.w(TAG, businessMessage, error);
        Snackbar.make(this.getContentView(), businessMessage, Snackbar.LENGTH_LONG).show();
    }

    protected String getFullImageUrl(String path, Integer width) {
        final String imagesBaseUrl = TMDbPreferences.getInstance().getImagesBaseUrl(this);
        return String.format(Locale.getDefault(), "%sw%d/%s", imagesBaseUrl, width, path);
    }

    protected void replaceFragment(String fragmentName) {
        this.replaceFragment(fragmentName, null);
    }

    protected void replaceFragment(String fragmentName, Bundle bundle) {
        try {
            final Fragment fragment = Fragment.instantiate(this, fragmentName, bundle);
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final Fragment currentFragment = fragmentManager.findFragmentByTag(TAG);
            if (currentFragment == null || !fragmentName.equals(currentFragment.getClass().getName())) {
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment, TAG).commit();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private View getContentView() {
        return super.findViewById(android.R.id.content);
    }
}
