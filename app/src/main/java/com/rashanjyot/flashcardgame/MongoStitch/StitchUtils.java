package com.rashanjyot.flashcardgame.MongoStitch;


import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoDatabase;
//import com.mongodb.stitch.core.services.mongodb.remote.ChangeEvent;
import com.mongodb.stitch.core.auth.providers.userapikey.UserApiKeyCredential;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ChangeEventListener;
import com.mongodb.stitch.core.services.mongodb.remote.sync.DefaultSyncConflictResolvers;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ErrorListener;
import com.mongodb.stitch.core.services.mongodb.remote.sync.internal.ChangeEvent;
import com.rashanjyot.flashcardgame.BroadcastReceiver.NetworkStateReceiver;
import com.rashanjyot.flashcardgame.Model.User;


import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Set;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.rashanjyot.flashcardgame.Gen.Globals.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class StitchUtils {

    private static StitchUtils singleInstance=null;

    private final NetworkStateReceiver receiver = new NetworkStateReceiver();

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    private static final UserApiKeyCredential credentials = new UserApiKeyCredential(STITCH_API_KEY);
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private StitchAppClient client;
    private RemoteMongoClient remoteMongoClient;
    private RemoteMongoDatabase remoteMongoDatabase;
    private Context context;

    public void setupNetworkListener()
    {

        receiver.addListener(new NetworkStateReceiver.NetworkStateReceiverListener() {
            @Override
            public void networkAvailable() {
                if(singleInstance!=null && !singleInstance.getStitchAppClient().getAuth().isLoggedIn())
                {
                    singleInstance.loginStitch();
                }
            }

            @Override
            public void networkUnavailable() {
            }
        });
        context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public static StitchUtils getInstance()
    {
        if (singleInstance == null)
            singleInstance = new StitchUtils();

        return singleInstance;
    }

    public void init(Context context)
    {
        this.context=context;
        setupStitch();
        setupNetworkListener();
        if(!getStitchAppClient().getAuth().isLoggedIn())
        {
            try
            {
                loginStitch();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void loginStitch()
    {
        getStitchAppClient().getAuth().loginWithCredential(credentials);
    }



    public StitchAppClient getStitchAppClient()
    {
        return client;
    }

    public RemoteMongoClient getRemoteMongoClient()
    {
        return remoteMongoClient;
    }

    public RemoteMongoDatabase getRemoteMongoDatabase() {
        return remoteMongoDatabase;
    }

    public RemoteMongoCollection getUsersCollection() {
        return remoteMongoDatabase.getCollection(USER_COLLECTION);
//        return remoteMongoDatabase.getCollection(USER_COLLECTION).withCodecRegistry(pojoCodecRegistry);
//        return remoteMongoDatabase.getCollection(USER_COLLECTION, User.class).withCodecRegistry(pojoCodecRegistry);
    }


    private void setupStitch()
    {
        try {
            client = Stitch.initializeAppClient(STITCH_APP_CLIENT);
        }
        catch (Exception e)
        {
            client=Stitch.getAppClient(STITCH_APP_CLIENT);
        }
        finally {
            remoteMongoClient = client.getServiceClient(RemoteMongoClient.factory, STITCH_SERVICE_NAME);
            remoteMongoDatabase= remoteMongoClient.getDatabase(STITCH_DB);


            //setting up error and update handlers for remote-local conflict
            RemoteMongoCollection mongoCollection= getRemoteMongoDatabase().getCollection(USER_COLLECTION);
            class MyErrorListener implements ErrorListener {
                @Override
                public void onError(BsonValue documentId, Exception error) {
                    Log.e("Stitch", error.getLocalizedMessage());
                    Set<BsonValue> docsThatNeedToBeFixed = mongoCollection.sync().getPausedDocumentIds();
                    for (BsonValue doc_id : docsThatNeedToBeFixed) {
                        // Add your logic to inform the user.
                        // When errors have been resolved, call
                        mongoCollection.sync().resumeSyncForDocument(doc_id);
                    }
                    // refresh the app view, etc.
                }
            }
            class MyUpdateListener implements ChangeEventListener<Document> {
                @Override
                public void onEvent(final BsonValue documentId, final ChangeEvent<Document> event) {
                    Log.v("RJ","happened");
                    if (!event.hasUncommittedWrites()) {
                        // Custom actions can go here
                    }
                    // refresh the app view, etc.
                }

            }
            mongoCollection.sync().configure(DefaultSyncConflictResolvers.localWins(), new MyUpdateListener(), new MyErrorListener());
        }
    }









}


