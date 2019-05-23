package com.example.eventrack;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.eventrack.R.id.reg_btn;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_email_field;
    private EditText reg_pass_field;
    private EditText reg_confirm_pass_field;
    private EditText reg_phone;
    private Button regBtn;
    private Button regLoginBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();

        reg_email_field= findViewById(R.id.reg_email);
        reg_pass_field=findViewById(R.id.reg_pass);
        reg_confirm_pass_field= findViewById(R.id.reg_confim_pass);
        reg_phone= findViewById(R.id.reg_phone);
        regBtn= findViewById(R.id.reg_btn);
        regLoginBtn= findViewById(R.id.reg_login_btn);


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=reg_email_field.getText().toString();
                String pass=reg_pass_field.getText().toString();
                String confirm_pass=reg_confirm_pass_field.getText().toString();
                String mobile=reg_phone.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirm_pass) && !TextUtils.isEmpty(mobile)){

                    if(pass.equals(confirm_pass)){
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    sendToMain();

                                }else{

                                    String errorMessage=task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"Error: "+errorMessage,Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }else{
                        Toast.makeText(RegisterActivity.this,"Confirm Password and Password field doesn't match",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        regLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            sendToMain();
        }

    }

    private void sendToMain() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
