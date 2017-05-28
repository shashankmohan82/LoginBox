package login.tagbox.task.loginbox.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import login.tagbox.task.loginbox.R;
import login.tagbox.task.loginbox.utils.LocationInfo;
import login.tagbox.task.loginbox.utils.NotificationHandler;
import login.tagbox.task.loginbox.utils.PasswordEncryption;
import login.tagbox.task.loginbox.utils.SessionHandler;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class LoginActivity extends AppCompatActivity  {

    private static final int REQUEST_LOCATION_PERMISSION = 0;
    private static final int USER_AUTHENTICATED = 200;
    private static final int USER_DETAILS_MISMATCH = 412;
    private static final int BAD_REQUEST = 400;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final String REST_API_URL = "http://104.215.248.40:8080/restservice/v1/d2c/login";
    private static final String API_USERNAME_KEY = "user";
    private static final String API_PASSWORD_KEY = "pwd";
    private static final String API_TIMESTAMP_KEY = "timestamp";
    private static final String API_LOCATION_KEY = "loc";
    private static final String API_CONTENT_TYPE_HEADER_KEY = "Content-type";
    private static final String API_CONTENT_TYPE_HEADER_VALUE = "text/plain";
    private static final String API_CACHE_TYPE_HEADER_KEY = "cache-control";
    private static final String API_CACHE_TYPE_HEADER_VALUE = "no-cache";
    private UserLoginTask mAuthTask = null;
    private EditText mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CheckBox checkBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(SessionHandler.readSessionFlagFromPreference(this) == 1){
            Intent intent = new Intent(this,WelcomeActivity.class);
            startActivity(intent);
            finish();
        }

        checkBox = (CheckBox)findViewById(R.id.checkbox);
        mUserNameView = (EditText) findViewById(R.id.username);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mPasswordView = (EditText) findViewById(R.id.password);


        if( !SessionHandler.readUserNameFromPreference(this).equals("")){

            mUserNameView.setText(SessionHandler.readUserNameFromPreference(this));
            String password = SessionHandler.decryptPasswordFromKeyStore(this);
            if(password != null){
                mPasswordView.setText(password);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestLocationPermission();
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_SEND) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String userName = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(userName)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if(isNetworkConnected()) {
                showProgress(true);
                mAuthTask = new UserLoginTask(userName, password);
                mAuthTask.execute((Void) null);
            }
            else{
                Snackbar.make(mUserNameView, R.string.network_needed,Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUserName;
        private final String mPassword;

        UserLoginTask(String userName, String password) {
            mUserName = userName;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final OkHttpClient client = new OkHttpClient();
            HashMap<String,String> jsonMap = new HashMap<>();

            LocationInfo locationInfo = new LocationInfo(getBaseContext());
            Calendar calendar = Calendar.getInstance();

            jsonMap.put(API_USERNAME_KEY,mUserName);
            jsonMap.put(API_PASSWORD_KEY,mPassword);
            if(locationInfo.getLocation() != null) {
                jsonMap.put(API_LOCATION_KEY, locationInfo.getLocation().getLatitude() + ";" +
                        " " + locationInfo.getLocation().getLongitude());
            }
            else{
                jsonMap.put(API_LOCATION_KEY, "");
            }
            jsonMap.put(API_TIMESTAMP_KEY,calendar.getTime()+"");

            JSONObject obj = new JSONObject(jsonMap);
            final String requestBody = obj.toString();

            Log.d("requestBody",requestBody);

            MediaType mediaType = MediaType.parse(API_CONTENT_TYPE_HEADER_VALUE);

            RequestBody body = RequestBody.create(mediaType, requestBody);
            Request request = new Request.Builder()
                    .url(REST_API_URL)
                    .post(body)
                    .addHeader(API_CONTENT_TYPE_HEADER_KEY, API_CONTENT_TYPE_HEADER_VALUE)
                    .addHeader(API_CACHE_TYPE_HEADER_KEY, API_CACHE_TYPE_HEADER_VALUE)
                    .build();

            try {

                Response response = client.newCall(request).execute();
                handleResponse(response.code(),mUserName,mPassword);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    public void handleResponse(int code,String userName, String password) {

        switch (code) {
            case BAD_REQUEST:
                Snackbar.make(mUserNameView, R.string.bad_request,Snackbar.LENGTH_SHORT).show();
                break;
            case USER_AUTHENTICATED:

                if(checkBox.isChecked()){
                    SessionHandler.writeSessionFlagToPreference(this,1);
                    SessionHandler.writeUserNameToPreference(this,userName);
                    SessionHandler.encryptPasswordUsingKeyStore(this,password);
                }
                else{
                    SessionHandler.writeSessionFlagToPreference(this,0);
                    SessionHandler.writeUserNameToPreference(this,"");
                }
                Intent intent = new Intent(this,WelcomeActivity.class);
                startActivity(intent);
                finish();
                break;

            case USER_DETAILS_MISMATCH:
                NotificationHandler notificationHandler = new NotificationHandler(this);
                notificationHandler.showFailedSigninNotification();
                Snackbar.make(mUserNameView, R.string.user_credential_mismatch,Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry, new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mPasswordView.setText("");
                                mUserNameView.setText("");
                                mLoginFormView.findFocus();
                            }
                        }).show();
                break;

            case INTERNAL_SERVER_ERROR:
                Snackbar.make(mUserNameView, R.string.server_error,Snackbar.LENGTH_SHORT).show();
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocationPermission() {


        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            Snackbar.make(mUserNameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                        }
                    }).show();
        }
        else if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 //normal flow happens with location getting sent to  rest api
            }
            else{
                //empty location data is being sent to rest api
            }
        }
    }

}

