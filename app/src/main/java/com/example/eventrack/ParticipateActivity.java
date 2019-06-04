package com.example.eventrack;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ParticipateActivity extends AppCompatActivity {

    private Toolbar newPartToolbar;

    private EditText partFirstName;
    private EditText partLastName;
    private EditText partEmpId;
    private EditText partDept;
    private Button partBtn;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String blog_post_id;

    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participate);

        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        current_user_id=firebaseAuth.getCurrentUser().getUid();

        newPartToolbar=findViewById(R.id.new_part_toolbar);
        setSupportActionBar(newPartToolbar);
        getSupportActionBar().setTitle("Participation Form");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        partFirstName=findViewById(R.id.part_firstname);
        partLastName=findViewById(R.id.part_lastname);
        partDept=findViewById(R.id.part_department);
        partEmpId=findViewById(R.id.part_emp_id);
        partBtn=findViewById(R.id.part_btn);

        blog_post_id=getIntent().getStringExtra("blog_post_id");

        firebaseFirestore.collection("Posts/" + blog_post_id + "/Participations").orderBy("timestamp", Query.Direction.ASCENDING);


        partBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String firstname=partFirstName.getText().toString();
                final String lastname=partLastName.getText().toString();
                final String deptname=partDept.getText().toString();
                final String empId=partEmpId.getText().toString();

                if(!TextUtils.isEmpty(firstname) && !TextUtils.isEmpty(deptname) && !TextUtils.isEmpty(empId) && !TextUtils.isEmpty(lastname) ) {

                    Map<String,Object> partMap=new HashMap<>();
                    partMap.put("firstname",firstname);
                    partMap.put("lastname",lastname);
                    partMap.put("department name",deptname);
                    partMap.put("employee id",empId);
                    partMap.put("user_id",current_user_id);
                    partMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Participations").add(partMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(ParticipateActivity.this, "Error Participating : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(ParticipateActivity.this, "Participant was added", Toast.LENGTH_LONG).show();
                                Intent mainIntent=new Intent(ParticipateActivity.this,MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });


                }
            }
        });


    }
}
