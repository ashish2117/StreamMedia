package com.ashish2117.user.streammedia.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by user on 08-08-2018.
 */

public class Register extends Activity {
    private EditText username, name, password, emailtxt, confpass;
    private Button registerButton;
    private String user, nam, pass, email, conf;
    private TextView login;
    private FirebaseAuth mAuth;
    ProgressDialog pd;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        name = (EditText) findViewById(R.id.name);
        emailtxt = (EditText) findViewById(R.id.email);
        confpass = (EditText) findViewById(R.id.confpassword);
        registerButton = (Button) findViewById(R.id.registerButton);
        login = (TextView) findViewById(R.id.login);
        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();
                nam = name.getText().toString();
                conf = confpass.getText().toString();
                email = emailtxt.getText().toString();
                if (user.equals("")) {
                    username.setError("can't be blank");
                } else if (pass.equals("")) {
                    password.setError("can't be blank");
                } else if (!user.matches("[A-Za-z0-9]+")) {
                    username.setError("only alphabet or number allowed");
                } else if (user.length() < 5) {
                    username.setError("at least 5 characters long");
                } else if (pass.length() < 5) {
                    password.setError("at least 5 characters long");
                } else {
                    pd = new ProgressDialog(Register.this);
                    pd.setMessage("Loading...");
                    pd.show();

                    String url = "https://stream-audio-527dd.firebaseio.com/users.json";

                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            if(TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                Toast.makeText(Register.this, "Invalid Email!", Toast.LENGTH_LONG).show();
                                pd.dismiss();
                            }
                            else if (!pass.equals(conf)) {
                                Toast.makeText(Register.this, "Oops! Passwords didn't match!", Toast.LENGTH_LONG).show();
                                pd.dismiss();
                            }
                            else if (s.equals("null")) {
                                  createUser(email,pass);
                            } else {
                                try {
                                    JSONObject obj = new JSONObject(s);
                                    if (!obj.has(user)) {
                                        createUser(email,pass);
                                    } else {
                                        Toast.makeText(Register.this, "username already exists", Toast.LENGTH_LONG).show();
                                        pd.dismiss();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("" + volleyError);
                            pd.dismiss();
                        }
                    });

                    RequestQueue rQueue = Volley.newRequestQueue(Register.this);
                    rQueue.add(request);
                }
            }
        });

    }

    private void createUser(final String email, final String password)
    {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Firebase reference = new Firebase("https://stream-audio-527dd.firebaseio.com/");
                    addUsernameToUser(mAuth.getCurrentUser());
                    reference.child("users").child(user).child("password").setValue(pass);
                    reference.child("users").child(user).child("chatopen").setValue(false);
                    reference.child("users").child(user).child("name").setValue(nam);
                    reference.child("users").child(user).child("connection").child("connectedto").setValue("");
                    //reference.child("usernames").child(user).setValue(nam);
                    UserDetails.username = user;
                    databaseReference= FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/"+
                            UserDetails.username);
                    databaseReference.child("device").setValue(FirebaseInstanceId.getInstance().getToken());
                    databaseReference.child("id").setValue(mAuth.getCurrentUser().getUid());
                }else
                {
                    if(task.getException() instanceof FirebaseAuthUserCollisionException)
                    {
                        Toast.makeText(Register.this,"Email alredy registered!",Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                    else
                        Toast.makeText(Register.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        pd.dismiss();
                }
            }

        });
    }

    public void addUsernameToUser(final FirebaseUser userr)
    {
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder().setDisplayName(user).build();
        userr.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(Register.this, Friends.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                pd.dismiss();
                startActivity(intent);
                finish();
            }
        });
    }
}
