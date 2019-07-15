package com.rashanjyot.flashcardgame.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.internal.LinkedTreeMap;
import com.mongodb.client.model.Filters;
import com.mongodb.stitch.android.services.mongodb.remote.SyncFindIterable;
import com.rashanjyot.flashcardgame.Exception.IncompleteDataException;
import com.rashanjyot.flashcardgame.Gen.GsonUtils;
import com.rashanjyot.flashcardgame.GlobalApplication;
import com.rashanjyot.flashcardgame.Model.LoginRequest;
import com.rashanjyot.flashcardgame.Model.User;
import com.rashanjyot.flashcardgame.R;

import org.bson.BsonObjectId;
import org.bson.Document;

import static com.rashanjyot.flashcardgame.Gen.Globals.*;
import static com.rashanjyot.flashcardgame.GlobalApplication.getCurrentUser;
import static com.rashanjyot.flashcardgame.GlobalApplication.getRecoveryUser;


public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView registerLink;
    private ProgressDialog progress;
    private static final int SYNC_TIME_OUT = 12000;
    private static final int CHECK_INTERVAL = 300;
    private static int count=0;
    private Intent broadcastIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        broadcastIntent= new Intent(USER_DATA_SAVED_ACTION);
        broadcastIntent.putExtra(INTENT_META,0);
        progress = new ProgressDialog(LoginActivity.this);
        loginButton= (Button)findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try
                {
                    String username = ((EditText) findViewById(R.id.lmail)).getText().toString();
                    String password = ((EditText) findViewById(R.id.lpwd)).getText().toString();
                    LoginRequest loginRequest = LoginRequest.getInstance(username, password);
                    signIn(loginRequest);

                }
                catch (IncompleteDataException e)
                {
                    Toast.makeText(getApplicationContext(),"Please enter the "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),"Error signing in",Toast.LENGTH_LONG).show();
                }

            }
        });

        registerLink= (TextView)findViewById(R.id.registerLink);
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openRegisterActivity();
            }
        });



    }

    private void setLoggedInUser(Document userDocument) throws Exception
    {
        GlobalApplication.getStitchInstance().getUsersCollection().sync().find(((GlobalApplication)getApplication()).getIdBson()).first().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                if(o!=null)
                {
                    new AlertDialog.Builder(LoginActivity.this).setCancelable(false).setMessage("Do you wish to sync data TO or FROM this email?").setPositiveButton("TO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            userDocument.put(USER_PROGRESS_FIELD,((Document)o).get(USER_PROGRESS_FIELD));
                            syncEmail(userDocument);
                        }
                    }).setNegativeButton("FROM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            syncEmail(userDocument);
                        }
                    }).create().show();

                }
                else
                {
                    Toast.makeText(getApplicationContext(),"error finding doc",Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"error finding doc",Toast.LENGTH_SHORT).show();
                progress.dismiss();

            }
        });

    }


    public void signIn(LoginRequest loginRequest) throws Exception
    {
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        GlobalApplication.getStitchInstance().getUsersCollection().find(new Document().append(USERNAME_FIELD,loginRequest.getId()).append(PASSWORD_FIELD,loginRequest.getPassword())).first().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                if(o==null)
                {
                    Toast.makeText(getApplicationContext(),"Incorrect username or password",Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
                else
                {
                    //setcurrent user
                    try
                    {
                        setLoggedInUser((Document) o);
                    }
                    catch (Exception e)
                    {
                        progress.dismiss();
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"error while handling user occured, please restart",Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Please check internet connection",Toast.LENGTH_LONG).show();
                progress.dismiss();
            }
        });
    }

    public void syncEmail(Document userDocument)
    {


        GlobalApplication.getStitchInstance().getUsersCollection().sync().syncOne(new BsonObjectId(userDocument.getObjectId(ID_FIELD)));

        trySyncing(userDocument);

//        ((GlobalApplication)getApplication()).createTempUser(userDocument);

//        ((GlobalApplication)getApplication()).loadUserData(userDocument);
//        ((GlobalApplication)getApplication()).saveUserPrefs(us);

//        User user= GsonUtils.getGson().fromJson((userDocument).toJson(),User.class);
//        GlobalApplication.getStitchInstance().getUsersCollection().sync().syncOne(new BsonObjectId(user.get_id()));
//        GlobalApplication.getStitchInstance().getUsersCollection().sync().updateOne(new Document(ID_FIELD,user.get_id()),new Document(USER_PROGRESS_FIELD,userDocument.get(USER_PROGRESS_FIELD))).addOnSuccessListener(new OnSuccessListener() {
//            @Override
//            public void onSuccess(Object o) {
//                GlobalApplication.getStitchInstance().getUsersCollection().sync().deleteOne(Filters.eq(ID_FIELD,getCurrentUser().get_id()));
//                GlobalApplication.setCurrentUser(user);
//                ((GlobalApplication)getApplication()).saveCurrentUserProgress();
//                progress.dismiss();
//                Intent intent = new Intent(LoginActivity.this, HomeNavActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(),"error: could not sign in ",Toast.LENGTH_SHORT).show();
//                progress.dismiss();
//            }
//        });



//        User user= GsonUtils.getGson().fromJson((userDocument).toJson(),User.class);
//        GlobalApplication.getStitchInstance().getUsersCollection().sync().insertOne(userDocument).addOnSuccessListener(new OnSuccessListener() {
//            @Override
//            public void onSuccess(Object o) {
//                GlobalApplication.getStitchInstance().getUsersCollection().sync().deleteOne(Filters.eq(ID_FIELD,getCurrentUser().get_id()));
//                GlobalApplication.setCurrentUser(user);
//                ((GlobalApplication)getApplication()).saveCurrentUserProgress();
//                progress.dismiss();
//                Intent intent = new Intent(LoginActivity.this, HomeNavActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(),"error: could not sign in ",Toast.LENGTH_SHORT).show();
//                progress.dismiss();
//            }
//        });


    }

    public void trySyncing(Document userDocument)
    {
        count=0;
        syncFunc(userDocument);
    }

    public void syncFunc(final Document userDocument)
    {
        if(count<SYNC_TIME_OUT)
        {
            count+=CHECK_INTERVAL;
            new Handler().postDelayed(
                    new Runnable() {
                        public void run() {

                            GlobalApplication.getStitchInstance().getUsersCollection().sync().find(new Document(ID_FIELD,userDocument.getObjectId(ID_FIELD))).first().addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    if(o!=null)
                                    {
                                        User user= GsonUtils.getGson().fromJson((userDocument).toJson(),User.class);
                                        User prevUser=getCurrentUser();
                                        try
                                        {
                                            GlobalApplication.setRecoveryUser(GlobalApplication.getCurrentUser());
                                            GlobalApplication.setCurrentUser(user);
                                            GlobalApplication.getStitchInstance().getUsersCollection().sync().updateOne(Filters.eq(ID_FIELD,getCurrentUser().get_id()),new Document("$set",new Document(USER_PROGRESS_FIELD,Document.parse(GsonUtils.getGson().toJson(getCurrentUser().getUserProgress()))))).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    Log.v("RJ",task.isComplete() +"  "+task.isSuccessful());
                                                    sendBroadcast(broadcastIntent);
                                                    ((GlobalApplication)getApplication()).saveUserPrefs(userDocument);
                                                    GlobalApplication.getStitchInstance().getUsersCollection().sync().deleteOne(Filters.eq(ID_FIELD,prevUser.get_id()));
                                                    progress.dismiss();
                                                    Intent intent = new Intent(LoginActivity.this, HomeNavActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(getApplicationContext(),"Save failed, rolling back to previous user stare", Toast.LENGTH_LONG).show();
                                                    GlobalApplication.setCurrentUser(GlobalApplication.getRecoveryUser());
                                                    progress.dismiss();

                                                }
                                            });
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                            Toast.makeText(getApplicationContext(),"Save failed, rolling back to previous user stare", Toast.LENGTH_LONG).show();
                                            GlobalApplication.setCurrentUser(GlobalApplication.getRecoveryUser());
                                            progress.dismiss();

                                        }

//                                        ((GlobalApplication)getApplication()).saveCurrentUserProgress();
//                                        progress.dismiss();



//                                        GlobalApplication.getStitchInstance().getUsersCollection().sync().updateOne(Filters.eq(ID_FIELD,userDocument.getObjectId(ID_FIELD)),new Document("$set",new Document(USER_PROGRESS_FIELD,userDocument.get(USER_PROGRESS_FIELD)))).addOnSuccessListener(new OnSuccessListener() {
//                                            @Override
//                                            public void onSuccess(Object o) {
//                                                ((GlobalApplication)getApplication()).saveUserPrefs(userDocument);
//                                                User user= GsonUtils.getGson().fromJson((userDocument).toJson(),User.class);
//                                                GlobalApplication.getStitchInstance().getUsersCollection().sync().deleteOne(Filters.eq(ID_FIELD,getCurrentUser().get_id()));
//                                                GlobalApplication.setCurrentUser(user);
//                                                ((GlobalApplication)getApplication()).saveCurrentUserProgress();
//                                                progress.dismiss();
//                                                Intent intent = new Intent(LoginActivity.this, HomeNavActivity.class);
//                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                startActivity(intent);
//                                                finish();
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                Toast.makeText(getApplicationContext(),"error: could not sign in ",Toast.LENGTH_SHORT).show();
//                                                progress.dismiss();
//                                            }
//                                        });



                                    }
                                    else
                                    {
                                        Log.v("RJ","not synced yet");
                                        syncFunc(userDocument);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    syncFunc(userDocument);

                                }
                            }).addOnCanceledListener(new OnCanceledListener() {
                                @Override
                                public void onCanceled() {
                                    syncFunc(userDocument);

                                }
                            });
                        }
                    },
                    CHECK_INTERVAL);
        }
        else
        {
            progress.dismiss();
            Toast.makeText(getApplicationContext(),"Timed out while loading data for user, please restart",Toast.LENGTH_LONG).show();
        }
    }





    public void openRegisterActivity()
    {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
        finish();
    }



}
