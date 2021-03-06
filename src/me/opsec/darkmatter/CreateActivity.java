package me.opsec.darkmatter;

import me.opsec.darkmatter.service.DarkService;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user.
 * 
 * TODO: Based on automatically created Login form, so some cleanup is needed.
 */
public class CreateActivity extends Activity {

    private EditText mPassword1View;
    private EditText mPassword1ConfirmView;
    private EditText mVolumeSizeView;
    private EditText mVolumePathView;
    private View mLoginFormView;
    private View mCreateStatusView;
    private TextView mStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create);

        mPassword1View = (EditText) findViewById(R.id.password1);
        mPassword1ConfirmView = (EditText) findViewById(R.id.password1_confirm);
        mVolumeSizeView = (EditText) findViewById(R.id.volume_size);
        mVolumePathView = (EditText) findViewById(R.id.volume_path);
        if (mVolumePathView.getText().toString().length() == 0) {
            mVolumePathView.setText("/sdcard/volume.dat");
        }
        mVolumePathView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptCreateVolume();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mCreateStatusView = findViewById(R.id.create_status);
        mStatusMessageView = (TextView) findViewById(R.id.status_message);

        findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateVolume();
            }
        });
    }

    /**
     * Attempts to create the volume. If there are form errors (missing fields, etc.), the errors are presented and no
     * actual create attempt is made.
     */
    public void attemptCreateVolume() {

        mPassword1View.setError(null);
        mPassword1ConfirmView.setError(null);
        mVolumeSizeView.setError(null);
        mVolumePathView.setError(null);

        String password1 = mPassword1View.getText().toString();

        int volumeSize = 2048;
        if (!TextUtils.isEmpty(mVolumeSizeView.getText())) {
            volumeSize = Integer.parseInt(mVolumeSizeView.getText().toString());
        }

        String volumePath = mVolumePathView.getText().toString();

        EditText errorView = null;

        if (TextUtils.isEmpty(volumePath)) {
            mVolumePathView.setError(getString(R.string.error_field_required));
            errorView = mVolumePathView;
        } else if (TextUtils.isEmpty(password1)) {
            mPassword1View.setError(getString(R.string.error_field_required));
            errorView = mPassword1View;
        } else if (!password1.equals(mPassword1ConfirmView.getText().toString())) {
            mPassword1View.setError(getString(R.string.error_password_mismatch));
            mPassword1ConfirmView.setError(getString(R.string.error_password_mismatch));
            mPassword1View.setText("");
            mPassword1ConfirmView.setText("");
            errorView = mPassword1View;
        }

        if (errorView != null) {
            errorView.requestFocus();
        } else {
            mStatusMessageView.setText(R.string.progress_creating);
            showProgress(true);
            Intent intent = new Intent(this, DarkService.class);
            intent.setAction(DarkService.ACTION_CREATE);
            intent.putExtra(DarkService.EXTRA_VOLUME_PATH, volumePath);
            intent.putExtra(DarkService.EXTRA_SIZE, volumeSize);
            intent.putExtra(DarkService.EXTRA_PASS, password1);
            startService(intent);
            finish();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCreateStatusView.setVisibility(View.VISIBLE);
            mCreateStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mCreateStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mCreateStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
