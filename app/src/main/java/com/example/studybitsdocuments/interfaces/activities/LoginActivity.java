package com.example.studybitsdocuments.interfaces.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studybitsdocuments.R;
import com.example.studybitsdocuments.Seeder;
import com.example.studybitsdocuments.utils.IndyWallet;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import java.util.concurrent.ExecutionException;

/**
 * A login screen that offers login via HyperLedger Indy Wallets
 */
public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "SB-Documents";


    /**
     * Initialize Indy system libraries
     */
    static {
        System.loadLibrary("indy");
    }

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserView = findViewById(R.id.user);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(view -> attemptLogin());

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.seed_menu, menu);
        return true;
    }



    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid user, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid user.
        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isUserValid(user)) {
            mUserView.setError(getString(R.string.error_invalid_user));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(this, user, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUserValid(String user) {
        return user.matches("([a-z]|[A-Z])([a-z]|[A-Z])*");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void seed(MenuItem item) {
        Seeder.seed(this)
               .thenRun(() -> Toast.makeText(getApplicationContext(), R.string.seedToast, Toast.LENGTH_SHORT).show());
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    public class UserLoginTask extends AsyncTask<Void, Void, String> {
        private final String mUser;
        private final String mPassword;
        private final Activity mActivity;

        UserLoginTask(Activity activity, String user, String password) {
            mUser = user;
            mPassword = password;
            mActivity = activity;
        }

        @Override
        protected String doInBackground(Void... params) {
            try(
                IndyWallet indyWallet = IndyWallet.isBuild() ? IndyWallet.getInstance() : new IndyWallet.Builder()
                                                                                                .name(mUser + "Wallet")
                                                                                                .seed(mPassword)
                                                                                                .path(Environment.getExternalStorageDirectory().getAbsolutePath() + "/indy_client/")
                                                                                                .build()
            ) {
                //TODO please don't use .get() ...twice...
                return Did.getListMyDidsWithMeta(indyWallet.unlock(mActivity).get()).get().replaceAll("^.|.$", "");

            } catch (IndyException | ExecutionException e) {
                Log.e(TAG, e.getMessage());
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
                Thread.currentThread().interrupt();
            }
           return null;
        }

        @Override
        protected void onPostExecute(String result) {

            mAuthTask = null;
            showProgress(false);

            if (result != null) {
                finish();
                Intent intent = new Intent(mActivity, MainActivity.class);
                intent.putExtra("DATA", result);
                mActivity.startActivity(intent);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            IndyWallet.clean();
            showProgress(false);
        }
    }

}

