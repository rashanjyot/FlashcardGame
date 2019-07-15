package com.rashanjyot.flashcardgame.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rashanjyot.flashcardgame.Activity.GameActivity;
import com.rashanjyot.flashcardgame.Activity.HomeNavActivity;
import com.rashanjyot.flashcardgame.Activity.SplashActivity;
import com.rashanjyot.flashcardgame.Model.CardProgress;
import com.rashanjyot.flashcardgame.Model.Deck;
import com.rashanjyot.flashcardgame.Model.DeckProgress;
import com.rashanjyot.flashcardgame.Model.User;
import com.rashanjyot.flashcardgame.Model.UserProgress;
import com.rashanjyot.flashcardgame.R;

import java.util.ArrayList;
import java.util.HashMap;

import static com.rashanjyot.flashcardgame.Gen.Globals.DECK_INTENT_EXTRA;

public class DeckRecyclerAdapter extends RecyclerView.Adapter<DeckRecyclerAdapter.DeckViewHolder> {
    private Activity activity;
    private ArrayList<Deck> deckList;
    private User user;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class DeckViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cardView;
        public DeckViewHolder(CardView cardView) {
            super(cardView);
            this.cardView=cardView;
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public DeckRecyclerAdapter(Activity activity,ArrayList<Deck> deckList, User user) {
        this.activity=activity;
        this.deckList = deckList;
        this.user=user;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DeckRecyclerAdapter.DeckViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.deck_card_layout, parent, false);
        DeckViewHolder vh = new DeckViewHolder(cardView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DeckViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ((TextView)holder.cardView.findViewById(R.id.deckName)).setText(deckList.get(position).getName());
        DeckProgress deckProgress=user.getUserProgress().getDeckWiseProgressMap().get(deckList.get(position).getDeckId());

        if(deckProgress.isCompleted())
        {
            ((TextView)holder.cardView.findViewById(R.id.completedDeckOnce)).setText("Completed");
            holder.cardView.findViewById(R.id.playDeckButton).setVisibility(View.GONE);
        }
        else
        {
            ((TextView)holder.cardView.findViewById(R.id.completedDeckOnce)).setText("Incomplete");
            holder.cardView.findViewById(R.id.playDeckButton).setVisibility(View.VISIBLE);
        }

        //computing score
        ((TextView)holder.cardView.findViewById(R.id.score)).setText(String.valueOf(deckProgress.getScore()));
        holder.cardView.findViewById(R.id.playDeckButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(activity, GameActivity.class);
                i.putExtra(DECK_INTENT_EXTRA,deckList.get(position));
                activity.startActivity(i);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return deckList.size();
    }

}