package com.inochi.firebase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.inochi.firebase.helper.BundleSetting;
import com.inochi.firebase.helper.Constants;
import com.inochi.firebase.item.UserItem;
import com.inochi.firebase.listener.FirestoreDatabaseListener;
import com.inochi.firebase.util.FirestoreDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class SplashActivity extends BaseActivity implements FirestoreDatabaseListener {
    private BundleSetting bundleSetting;
    private GoogleSignInClient googleSignInClient;
    private FirestoreDatabase firestoreDatabase;
    private FirebaseAuth firebaseAuth;

    private UserItem userItemLogin;
    private UserItem userItemLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        bundleSetting = new BundleSetting(this);
        firestoreDatabase = new FirestoreDatabase(this);
        firestoreDatabase.setFirestoreDatabaseListener(this);

        TextView tvwDescription = findViewById(R.id.tvwDescription);
        tvwDescription.setVisibility(View.GONE);
        tvwDescription.setText("");

        try {
            TextView tvwVersion = findViewById(R.id.tvwVersion);
            if (tvwVersion != null){
                String versionName = BuildConfig.VERSION_NAME;
                tvwVersion.setText(versionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            setPermission();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Constants.Permission.SIGN_IN) {
            //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        String idToken = acct.getIdToken();
        if (idToken == null) idToken = "";

        if (!idToken.isEmpty()){
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        FirebaseUser currentUser = null;
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = firebaseAuth.getCurrentUser();
                                updateUI(currentUser);
                            }
                        }
                    });
        }
    }

    @SuppressLint("InlinedApi")
    private void setPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.Permission.READ_STORAGE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.Permission.WRITE_STORAGE);
        } else {
            prepareLogin();
        }
    }

    private boolean isNetworkConnected() {
        try {
            ConnectivityManager connectivityManager = ((ConnectivityManager)
                    this.getSystemService(Context.CONNECTIVITY_SERVICE));
            return connectivityManager.getActiveNetworkInfo() != null
                    && connectivityManager.getActiveNetworkInfo().isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void prepareLogin(){
        userItemLast = bundleSetting.getUserItemLast();
        if (!userItemLast.getUserId().isEmpty()){
            beforeShowMain();
        } else {
            boolean isConnected;
            isConnected = isNetworkConnected();
            if (isConnected){
                showProgressDialog("Login to Google...");
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.web_id_client))
                        .requestEmail()
                        .build();

                googleSignInClient = GoogleSignIn.getClient(this, gso);

                firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser == null){
                    signIn();
                } else {
                    updateUI(currentUser);
                }
            } else {
                beforeShowMain();
            }
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Constants.Permission.SIGN_IN);
    }

    private void updateUI(final FirebaseUser user) {
        if (user != null){
            userItemLogin = new UserItem();
            userItemLogin.setUserName(user.getDisplayName());
            userItemLogin.setUserId(user.getUid());
            userItemLogin.setUserEmail(user.getEmail());
            userItemLogin.setUserPhoto("");

            String version = "";
            PackageInfo pInfo;
            try {
                pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
                version = String.valueOf(pInfo.versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            userItemLogin.setAppVer(version);

            Uri userPhotoUri = user.getPhotoUrl();
            if (userPhotoUri != null){
                userItemLogin.setUserPhoto(userPhotoUri.toString());
            }

            userItemLast = bundleSetting.getUserItemLast();

            try {
                if (!userItemLast.getUserId().equals(userItemLogin.getUserId())){
                    firestoreDatabase.getUser(userItemLogin);
                } else {
                    beforeShowMain();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void beforeShowMain(){
        hideProgressDialog();
        userItemLast = bundleSetting.getUserItemLast();

        boolean isConnected = isNetworkConnected();

        if (isConnected)
            firestoreDatabase.createUserListener(userItemLast);

        if (userItemLast.getUserType() == Constants.User.Type.BAN){
            new AlertDialog.Builder(SplashActivity.this)
                    .setTitle("Banned")
                    .setMessage("Your account already banned because some reason")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            showMainActivity();
        }
    }

    private void showMainActivity(){
        try {
            Bundle args = getIntent().getExtras();
            if (args == null) args = new Bundle();

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtras(args);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFirestoreDataSuccess(int dataType, Object value) {
        if (value != null){
            switch (dataType){
                case Constants.Firestore.Data.CREATE_USER:
                    UserItem item = (UserItem) value;
                    firestoreDatabase.getUser(item);
                    break;
            }
        }
    }

    @Override
    public void onFirestoreDataComplete(int dataType, ArrayList<?> result) {
        switch (dataType){
            case Constants.Firestore.Data.GET_USER:
                ArrayList<UserItem> userItems = (ArrayList<UserItem>) result;
                if (userItems.size() == 0){
                    Calendar currDay = Calendar.getInstance();
                    long curTime = currDay.getTimeInMillis();
                    userItemLogin.setUserDate(curTime);
                    firestoreDatabase.createUser(userItemLogin);
                } else {
                    userItemLogin = userItems.get(0);
                    bundleSetting.setUserItemLast(userItemLogin);
                    beforeShowMain();
                }
                break;
        }
    }
}
