package com.rashanjyot.flashcardgame.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.rashanjyot.flashcardgame.Exception.IncompleteDataException;
import com.rashanjyot.flashcardgame.GlobalApplication;
import com.rashanjyot.flashcardgame.Model.RegisterRequest;
import com.rashanjyot.flashcardgame.R;

import org.bson.Document;

import static com.rashanjyot.flashcardgame.Gen.Globals.*;


public class RegisterActivity extends AppCompatActivity {

    private TextView loginLink;
    private Button registerButton;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        progress = new ProgressDialog(RegisterActivity.this);

        loginLink = (TextView) findViewById(R.id.loginLink);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openLoginActivity();
            }
        });

        registerButton= (Button)findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    String username = ((EditText) findViewById(R.id.username)).getText().toString();
                    String password = ((EditText) findViewById(R.id.rpwd)).getText().toString();
                    String confirmPassword = ((EditText) findViewById(R.id.rcpwd)).getText().toString();

                    RegisterRequest registerRequest = RegisterRequest.getInstance(username, password, confirmPassword);
                    registerUser(registerRequest);



                }
                catch (IncompleteDataException e)
                {
                    Toast.makeText(getApplicationContext(),"Please enter "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"could not create new user record",Toast.LENGTH_SHORT).show();
                    progress.dismiss();

                }



            }
        });
    }


    public void openLoginActivity()
    {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void registerUser(RegisterRequest registerRequest) throws Exception
    {
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        Document doc=GlobalApplication.getNewTempUser();
        doc.put(USERNAME_FIELD,registerRequest.getUsername());
        doc.put(PASSWORD_FIELD,registerRequest.getPassword());
        doc.put(TEMP_USER_FIELD,false);
        GlobalApplication.getStitchInstance().getUsersCollection().insertOne(doc).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(getApplicationContext(),"Successfully registered",Toast.LENGTH_SHORT).show();
                openLoginActivity();
                progress.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                //temp string contain check for UI purpose only to let user know about pre-existing user
                if(e.getMessage()!=null && e.getMessage().contains("duplicate key error"))
                {
                    Toast.makeText(getApplicationContext(),"Username already registered",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please check internet connection",Toast.LENGTH_SHORT).show();
                }
                progress.dismiss();

            }
        });
    }
}
