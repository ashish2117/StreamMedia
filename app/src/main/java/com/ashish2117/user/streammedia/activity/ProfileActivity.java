package com.ashish2117.user.streammedia.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ashish2117.user.streammedia.util.ImageLoadTask;
import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.util.ProfileImageHandler;
import com.ashish2117.user.streammedia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    TextView changeDp;
    FirebaseAuth mAuth;
    Uri imageUri;
    final int PICK_IMAGE_REQUEST = 111;
    CircleImageView dpImageView;
    ProgressDialog pd;
    TextView saveButton;
    Button backButton;
    EditText nameText;
    EditText statusText;
    RadioButton maleRadio, femaleRadio, otherRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        changeDp = (TextView) findViewById(R.id.changeDp);
        mAuth = FirebaseAuth.getInstance();
        OnlineStatusHandler.goingToBackGround = true;

        dpImageView = findViewById(R.id.dpImageView);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        statusText = (EditText) findViewById(R.id.statusText);
        nameText = (EditText) findViewById(R.id.nameText);
        maleRadio = findViewById(R.id.maleRadio);
        femaleRadio = findViewById(R.id.femaleRadio);
        otherRadio = findViewById(R.id.otherRadio);

        FirebaseDatabase.getInstance().
                getReference().child("users").child(mAuth.getCurrentUser().getDisplayName())
                .child("profile_data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    Map<String,String> profileData = (Map<String, String>)dataSnapshot.getValue();
                    nameText.setText(profileData.get("name"));
                    statusText.setText(profileData.get("status"));
                    setGender(profileData.get("gender"));
                }else
                     maleRadio.setChecked(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setProfilePicture();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineStatusHandler.goingToBackGround = false;
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkConnected()){
                    Map<String,String> profileData = new HashMap<>();
                    profileData.put("name",nameText.getText().toString());
                    profileData.put("status",statusText.getText().toString());
                    profileData.put("gender",getGender());
                    FirebaseDatabase.getInstance().
                            getReference().child("users").child(mAuth.getCurrentUser().getDisplayName())
                            .child("profile_data").setValue(profileData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this,"Saved",Toast.LENGTH_SHORT).show();
                                finish();
                                OnlineStatusHandler.goingToBackGround = false;
                            }
                        }
                    });
                }else{
                    Toast.makeText(ProfileActivity.this,"No network connection!",Toast.LENGTH_SHORT).show();
                }

            }
        });

        dpImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineStatusHandler.goingToBackGround = false;
                Intent intent=new Intent(ProfileActivity.this, FriendProfileActivity.class);
                intent.putExtra("friend",mAuth.getCurrentUser().getDisplayName());
                startActivity(intent);
            }
        });

        changeDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog=new Dialog(ProfileActivity.this);
                dialog.setContentView(R.layout.change_dp_popup);
                TextView newDp=(TextView)dialog.findViewById(R.id.newDp);
                final TextView removeDp = (TextView)dialog.findViewById(R.id.removeDp);
                newDp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pickNewDp();
                        dialog.dismiss();
                    }
                });

                removeDp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDp();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }

    private void setGender(String gender) {
        if(gender.equals("male"))
            maleRadio.setChecked(true);
        else if(gender.equals("female"))
            femaleRadio.setChecked(true);
        else
            otherRadio.setChecked(true);
    }

    private String getGender() {
        if(maleRadio.isChecked())
            return "male";
        if(femaleRadio.isChecked())
            return "female";
        return "other";
    }

    private void setProfilePicture() {
        if(ProfileImageHandler.isImageExists(this,mAuth.getCurrentUser().getDisplayName() + ".jpg")){
            ProfileImageHandler.setImage(mAuth.getCurrentUser().getDisplayName() + ".jpg",dpImageView,this);
            return;
        }
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getDisplayName())
                .child("profile_pic_uri")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null) {
                            ImageLoadTask loadTask = new ImageLoadTask(dataSnapshot.getValue().toString(), dpImageView,
                                    ProfileActivity.this,mAuth.getCurrentUser().getDisplayName() + ".jpg");
                            loadTask.execute();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void removeDp() {
    }

    private void pickNewDp() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        OnlineStatusHandler.goingToBackGround = false;
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    private void uploadNewDp(){
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        File file = new File(imageUri.getPath());
        StorageReference riversRef = mStorageRef.child("profile_pics").child(file.getName());
        pd = new ProgressDialog(this);
        pd.setMessage("Uploading..");
        pd.show();
        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        pd.dismiss();
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        FirebaseDatabase.getInstance().getReference().child("users").
                                child(mAuth.getCurrentUser().getDisplayName()).child("profile_pic_uri")
                                .setValue(downloadUrl.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    try {
                                        //getting image from gallery
                                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                        dpImageView.setImageBitmap(bitmap);
                                        ProfileImageHandler.storeImage(bitmap,ProfileActivity.this,mAuth.getCurrentUser().getDisplayName() + ".jpg");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(ProfileActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                                }else
                                    Toast.makeText(ProfileActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        pd.dismiss();
                        Toast.makeText(ProfileActivity.this,"Error",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.setMessage("Uploading..." + (taskSnapshot.getBytesTransferred() * 100)/taskSnapshot.getTotalByteCount() + "%");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadNewDp();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        OnlineStatusHandler.setOnline();
        OnlineStatusHandler.goingToBackGround = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(OnlineStatusHandler.goingToBackGround)
        OnlineStatusHandler.setOffline();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        OnlineStatusHandler.goingToBackGround = false;
    }
}
