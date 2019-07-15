package com.rashanjyot.flashcardgame.Model;

import com.rashanjyot.flashcardgame.Gen.GsonUtils;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;

public class UserProgress {

    private HashMap<Integer,DeckProgress> deckWiseProgressMap;

    UserProgress()
    {
        deckWiseProgressMap=new HashMap<>();
    }

    public HashMap<Integer, DeckProgress> getDeckWiseProgressMap() {
        return deckWiseProgressMap;
    }

    public void setDeckWiseProgressMap(HashMap<Integer, DeckProgress> deckWiseProgressMap) {
        this.deckWiseProgressMap = deckWiseProgressMap;
    }

    public static UserProgress getZeroProgressInstance(ArrayList<Deck> deckList)
    {
        UserProgress userProgress=new UserProgress();
        for(Deck deck: deckList)
        {

            DeckProgress deckProgress= new DeckProgress();
            deckProgress.setDeckId(deck.getDeckId());
            userProgress.getDeckWiseProgressMap().put(deck.getDeckId(),deckProgress);
            for(Card card: deck.getCardMap().values())
            {
                CardProgress cardProgress= new CardProgress();
                cardProgress.setCardId(card.getCardId());
                deckProgress.getCardWiseProgressMap().put(card.getCardId(),cardProgress);
            }
        }
        return userProgress;
    }

    @Override
    public String toString() {
        return GsonUtils.getGson().toJson(this).toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
