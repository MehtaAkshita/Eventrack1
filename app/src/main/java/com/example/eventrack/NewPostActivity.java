package com.example.eventrack;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH=100;

    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private EditText newPostDesc;
    private EditText newPostVenue;
    private EditText newPostDate;
    private EditText newPostTime;
    private Button newPostBtn;

    private Uri postImageUri=null;

    private ProgressBar newPostProgress;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        current_user_id=firebaseAuth.getCurrentUser().getUid();

        newPostToolbar=findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage=findViewById(R.id.new_post_image);
        newPostDesc=findViewById(R.id.new_post_desc);
        newPostVenue=findViewById(R.id.new_post_venue);
        newPostDate=findViewById(R.id.new_post_date);
        newPostTime=findViewById(R.id.new_post_time);
        newPostBtn=findViewById(R.id.post_btn);
        newPostProgress=findViewById(R.id.new_post_progress);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String desc=newPostDesc.getText().toString();
                final String venue=newPostVenue.getText().toString();
                final String date=newPostDate.getText().toString();
                final String time=newPostTime.getText().toString();

                if(!TextUtils.isEmpty(desc) && !TextUtils.isEmpty(venue) && !TextUtils.isEmpty(date) && !TextUtils.isEmpty(time) && postImageUri!=null){
                    newPostProgress.setVisibility(View.VISIBLE);

                    final String randomName= UUID.randomUUID().toString();

                    StorageReference filePath=storageReference.child("post_images").child(randomName+".jpg");
                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            final String downloadUri=task.getResult().toString();

                            if(task.isSuccessful()){

                                File newImageFile=new File(postImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(2)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData=baos.toByteArray();

                                UploadTask uploadTask=storageReference.child("post_images/thumbs").child(randomName+".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();
                                        while (!urlTask.isSuccessful());
                                        final  String downloadthumbUri =urlTask.getResult().toString();

//                                        String downloadthumbUri=taskSnapshot.getUploadSessionUri().toString();

                                        Map<String,Object> postMap=new HashMap<>();
                                        postMap.put("image_url",downloadUri);
                                        postMap.put("image_thumb",downloadthumbUri);
                                        postMap.put("desc",desc);
                                        postMap.put("venue",venue);
                                        postMap.put("date",date);
                                        postMap.put("time",time);
                                        postMap.put("user_id",current_user_id);
                                        postMap.put("timestamp",FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful()){

                                                    Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                    Intent mainIntent=new Intent(NewPostActivity.this,MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                }else{

                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this, "Error" + error, Toast.LENGTH_LONG).show();
                                                }
                                                newPostProgress.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        /* Error Handling */
                                        Toast.makeText(NewPostActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }else{

                                newPostProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri=result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

//    public static String random() {
//        Random generator = new Random();
//        StringBuilder randomStringBuilder = new StringBuilder();
//        int randomLength = generator.nextInt(MAX_LENGTH);
//        char tempChar;
//        for (int i = 0; i < randomLength; i++){
//            tempChar = (char) (generator.nextInt(96) + 32);
//            randomStringBuilder.append(tempChar);
//        }
//        return randomStringBuilder.toString();
//    }

}
