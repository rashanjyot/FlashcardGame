package com.rashanjyot.flashcardgame;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.model.Filters;
import com.rashanjyot.flashcardgame.Activity.HomeNavActivity;
import com.rashanjyot.flashcardgame.Activity.LoginActivity;
import com.rashanjyot.flashcardgame.Activity.SplashActivity;
import com.rashanjyot.flashcardgame.Gen.GsonUtils;
import com.rashanjyot.flashcardgame.Model.Deck;
import com.rashanjyot.flashcardgame.Model.User;
import com.rashanjyot.flashcardgame.Model.UserProgress;
import com.rashanjyot.flashcardgame.MongoStitch.StitchUtils;

import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.function.Function;

import static com.rashanjyot.flashcardgame.Gen.Globals.DECK_JSON_ARRAY;

import static com.rashanjyot.flashcardgame.Gen.Globals.*;

public class GlobalApplication extends Application {

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private static StitchUtils stitchInstance;
    private static User currentUser;
    private static User recoveryUser;
    private Intent broadcastIntent;


    @Override
    public void onCreate() {
        super.onCreate();
        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();

        broadcastIntent= new Intent(USER_DATA_SAVED_ACTION);
        broadcastIntent.putExtra(INTENT_META,0);
    }

    public void init(SplashActivity splashActivity)
    {
        stitchInstance = StitchUtils.getInstance();
        stitchInstance.init(getApplicationContext());
        loadUserData(null);
    }

    public static StitchUtils getStitchInstance()
    {
        return stitchInstance;
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    public static User getCurrentUser(){
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        GlobalApplication.currentUser = currentUser;
    }

    public static User getRecoveryUser(){
        return recoveryUser;
    }

    public static void setRecoveryUser(User recoveryUser) {
        GlobalApplication.recoveryUser = recoveryUser;
    }

    public void loadUserData(Document userDocument)
    {
        getStitchInstance().getUsersCollection().sync().find(getIdBson()).first().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                if(o==null)
                {
                    createTempUser(userDocument);
                }
                else
                {
                    //setcurrent user
                    try
                    {
                        User user= GsonUtils.getGson().fromJson(((Document)o).toJson(),User.class);
                        setCurrentUser(user);
                        sendBroadcast(broadcastIntent);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"error while handling user occured, please restart",Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"error while handling user occured, please restart",Toast.LENGTH_LONG).show();
            }
        });
    }


    public void createTempUser(final Document userDocument)
    {
        try
        {
            Document doc;
            if(userDocument==null)
            {
                doc=getNewTempUser();
            }
            else
            {
                doc=userDocument;
            }
            getStitchInstance().getUsersCollection().sync().insertOne(doc).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    //write id to prefs
                    saveUserPrefs((Document)doc);
                    loadUserData(doc);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"could not create new user record, please restart app",Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"could not create new user record, please restart app",Toast.LENGTH_SHORT).show();
        }


    }

    public static Document getNewTempUser()throws Exception
    {
        User tempUser = new User();
        tempUser.setUserProgress(UserProgress.getZeroProgressInstance(GsonUtils.getGson().fromJson(DECK_JSON_ARRAY,new TypeToken<ArrayList<Deck>>(){}.getType())));
        Document tempDoc= Document.parse(GsonUtils.getGson().toJson(tempUser));

        tempDoc.append(ID_FIELD, ObjectId.get()).append(USERNAME_FIELD,tempDoc.getObjectId(ID_FIELD).toHexString()).append(PASSWORD_FIELD,"tempPassword");
        return tempDoc;

    }

    public void saveCurrentUserProgress()
    {
        try
        {
            sendBroadcast(broadcastIntent);
            recoveryUser= (User) currentUser.clone();
            getStitchInstance().getUsersCollection().sync().updateOne(Filters.eq(ID_FIELD,getCurrentUser().get_id()),new Document("$set",new Document(USER_PROGRESS_FIELD,Document.parse(GsonUtils.getGson().toJson(getCurrentUser().getUserProgress()))))).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    Log.v("RJ",task.isComplete() +"  "+task.isSuccessful());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Save failed, rolling back to previous user stare", Toast.LENGTH_LONG).show();
                    setCurrentUser(recoveryUser);
                    sendBroadcast(broadcastIntent);

                }
            });
        }
        catch (Exception e)
        {

        }
    }


//some issue, check later until then use syncSwap
    public void signOut()
    {
        getStitchInstance().getUsersCollection().sync().find(getIdBson()).first().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {

                try {
                    Document tempUserDoc = GlobalApplication.getNewTempUser();
                    tempUserDoc.put(USER_PROGRESS_FIELD, Document.parse(GsonUtils.getGson().toJson(getCurrentUser().getUserProgress())));

                    if(o!=null)
                    {
                        getStitchInstance().getUsersCollection().sync().desyncOne(new BsonObjectId(getCurrentUser().get_id()));
                    }
                    loadUserData(tempUserDoc);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"error signing out",Toast.LENGTH_SHORT).show();
            }
        });
    }

//    public void signOut()
//    {
//        getStitchInstance().getUsersCollection().sync().find().first().addOnSuccessListener(new OnSuccessListener() {
//            @Override
//            public void onSuccess(Object o) {
//
//                try {
//                    Document tempUserDoc = GlobalApplication.getNewTempUser();
//                    tempUserDoc.put(USER_PROGRESS_FIELD, Document.parse(GsonUtils.getGson().toJson(getCurrentUser().getUserProgress())));
//                    if (o != null)
//                    {
//                        syncSwap(tempUserDoc);
//                    }
//                    else
//                    {
//                        createTempUser(tempUserDoc);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(),"error signing out",Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    public void syncSwap(Document userDocument)
//    {
//        User user= GsonUtils.getGson().fromJson((userDocument).toJson(),User.class);
//        getStitchInstance().getUsersCollection().sync().insertOne(userDocument).addOnSuccessListener(new OnSuccessListener() {
//            @Override
//            public void onSuccess(Object o) {
//                getStitchInstance().getUsersCollection().sync().desyncOne(new BsonObjectId(getCurrentUser().get_id()));
//                GlobalApplication.setCurrentUser(user);
//                saveCurrentUserProgress();
//                saveUserPrefs(userDocument);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(),"error creating temp doc, trying again",Toast.LENGTH_SHORT).show();
//                signOut();
//            }
//        });
//    }

    public Document getIdBson()
    {
        Document d;
        if(preferences.getString(ID_FIELD,null)!=null)
        {
            d=new Document().append(ID_FIELD,new ObjectId(preferences.getString(ID_FIELD,null)));
        }
        else
        {
            d=new Document().append(ID_FIELD,new ObjectId());
        }
        return d;
    }


    public void saveUserPrefs(Document userDoc)
    {
        editor.putString(ID_FIELD,(userDoc).getObjectId(ID_FIELD).toHexString());
        editor.apply();
    }



}

