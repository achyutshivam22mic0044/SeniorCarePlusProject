package com.example.seniorcareplusproject;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQ_ONE_TAP = 100;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private TextView statusTextView;
    private EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       statusTextView = findViewById(R.id.textView);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button submitButton = findViewById(R.id.Submit);
        oneTapClient = Identity.getSignInClient(this);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if ("IIP".equals(username) && "IIP".equals(password)) {
                    Intent screen2 = new Intent(MainActivity.this, MainPage.class);
                    startActivity(screen2);
                    Toast.makeText(MainActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(
                        BeginSignInRequest.PasswordRequestOptions.builder()
                                .setSupported(true)
                                .build()
                )
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId("373406658169-vvf82elmq0aj8t72gsbklf01tiv8ibb8.apps.googleusercontent.com") // Replace with your real client ID
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                )
                .setAutoSelectEnabled(true)
                .build();
    }

    public void buttonGoogleSignIn(View view) {
        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(),
                                    REQ_ONE_TAP,
                                    null,
                                    0,
                                    0,
                                    0
                            );
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                            Toast.makeText(MainActivity.this, "Failed to launch Google Sign-In", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Sign-in failed: " + e.getLocalizedMessage());
                        Toast.makeText(MainActivity.this, "Sign-in attempt failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                String username = credential.getId();

                if (idToken != null) {
                    statusTextView.setText("Authentication successful. Welcome, " + username);
                    Log.d(TAG, "Received ID token.");
                    // Navigate to MainPage after successful sign-in
                    Intent intent = new Intent(MainActivity.this, MainPage.class);
                    startActivity(intent);
                    // Show toast message for successful sign-in
                    Toast.makeText(MainActivity.this, "Logged in successfully with Google!", Toast.LENGTH_LONG).show();

                }
            } catch (ApiException e) {
                Log.e(TAG, "Sign-in error: " + e.getLocalizedMessage());
                statusTextView.setText("Error during sign-in: " + e.getMessage());
            }
        }
    }
}