package com.ashish2117.user.streammedia.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by user on 08-08-2018.
 */

public class Login extends Activity {
    TextView registerUser,errorMsgTextView;
    EditText username, password;
    Button loginButton;
    String user, pass;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        registerUser = (TextView)findViewById(R.id.register);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        loginButton = (Button)findViewById(R.id.loginButton);
        errorMsgTextView = findViewById(R.id.errorMessageTextView);
        reference=FirebaseDatabase.getInstance().getReference();
        Firebase.setAndroidContext(this);
        mAuth=FirebaseAuth.getInstance();
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        View.OnFocusChangeListener listener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                   errorMsgTextView.setText("");
            }
        };

        username.setOnFocusChangeListener(listener);
        password.setOnFocusChangeListener(listener);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();

                if(TextUtils.isEmpty(user) || !android.util.Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
                    username.setError("can't be blank");
                } else if (pass.equals("")) {
                    password.setError("can't be blank");
                } else {
                    final ProgressDialog pd = new ProgressDialog(Login.this);
                    pd.setMessage("Loading...");
                    pd.show();


                    mAuth.signInWithEmailAndPassword(user,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                UserDetails.username = mAuth.getCurrentUser().getDisplayName();
                                Toast.makeText(Login.this, UserDetails.username, Toast.LENGTH_LONG).show();
                                reference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/" +
                                        UserDetails.username);
                                reference.child("device").setValue(FirebaseInstanceId.getInstance().getToken());
                                Intent intent = new Intent(Login.this, Friends.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                pd.dismiss();
                                startActivity(intent);
                                finish();
                            }else {
                                errorMsgTextView.setText("Invalid e-Mail ID or password!");
                                pd.dismiss();
                            }

                        }
                    });
                }

            }
        });
    }
}