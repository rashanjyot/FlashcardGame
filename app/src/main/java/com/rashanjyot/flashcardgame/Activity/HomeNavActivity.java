package com.rashanjyot.flashcardgame.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.reflect.TypeToken;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.rashanjyot.flashcardgame.Adapter.DeckRecyclerAdapter;
import com.rashanjyot.flashcardgame.Gen.GsonUtils;
import com.rashanjyot.flashcardgame.GlobalApplication;
import com.rashanjyot.flashcardgame.Model.Deck;
import com.rashanjyot.flashcardgame.Model.User;
import com.rashanjyot.flashcardgame.R;

import org.bson.BsonObjectId;
import org.bson.Document;
import org.w3c.dom.Text;


import java.util.ArrayList;

import static com.rashanjyot.flashcardgame.Gen.Globals.*;
import static com.rashanjyot.flashcardgame.GlobalApplication.getCurrentUser;

public class HomeNavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<Deck> deckList;
    private RecyclerView deckRecyclerView;
    private DeckRecyclerAdapter deckRecyclerAdapter;
    private RecyclerView.LayoutManager deckRecyclerLayoutManager;
    private BroadcastReceiver receiver;
    private NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        init();
        initDecks();

    }

    public void init()
    {

        deckRecyclerView = (RecyclerView) findViewById(R.id.deckListRecyclerView);
        deckRecyclerLayoutManager = new LinearLayoutManager(this);
        deckRecyclerView.setLayoutManager(deckRecyclerLayoutManager);

        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                deckRecyclerAdapter.setUser(getCurrentUser());
                deckRecyclerAdapter.notifyDataSetChanged();
                signInOutEval();


            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(USER_DATA_SAVED_ACTION);
        registerReceiver(receiver,filter);

        signInOutEval();


    }


    public void signInOutEval()
    {
        Menu nav_Menu = navigationView.getMenu();

        if(getCurrentUser().isTempUser())
        {
            nav_Menu.findItem(R.id.signIn).setVisible(true);
            nav_Menu.findItem(R.id.signOut).setVisible(false);
            ((TextView)navigationView.getHeaderView(0).findViewById(R.id.usernameText)).setText("Guest-"+getCurrentUser().getUsername());

        }
        else
        {
            ((TextView)navigationView.getHeaderView(0).findViewById(R.id.usernameText)).setText(getCurrentUser().getUsername());
            nav_Menu.findItem(R.id.signIn).setVisible(false);
            nav_Menu.findItem(R.id.signOut).setVisible(true);

        }

    }



    public void initDecks()
    {
        deserialiseDeckList();
        setupDeckRecycler();

    }

    public void deserialiseDeckList()
    {
        try {
            deckList= GsonUtils.getGson().fromJson(DECK_JSON_ARRAY,new TypeToken<ArrayList<Deck>>(){}.getType());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"error deserialising deck",Toast.LENGTH_SHORT).show();
            deckList= new ArrayList<>();
        }
    }

    public void setupDeckRecycler()
    {
        // specify an adapter (see also next example)
        deckRecyclerAdapter = new DeckRecyclerAdapter(HomeNavActivity.this,deckList,getCurrentUser());
        deckRecyclerView.setAdapter(deckRecyclerAdapter);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.signIn) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);

        } else if (id == R.id.signOut) {

            ((GlobalApplication)getApplication()).signOut();



        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
