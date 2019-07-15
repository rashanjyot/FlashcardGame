package com.rashanjyot.flashcardgame.Model;

import com.rashanjyot.flashcardgame.Gen.GsonUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class DeckProgress{

    private Integer deckId;
    private HashMap<Integer,CardProgress> cardWiseProgressMap;
    private boolean isCompleted=false;
    private static final int SCORE_COMPLETE=10;
    private int incompleteNewStreak=0;

    DeckProgress()
    {
        cardWiseProgressMap=new HashMap<>();
    }

    public Integer getDeckId() {
        return deckId;
    }

    public void setDeckId(Integer deckId) {
        this.deckId = deckId;
    }

    public HashMap<Integer, CardProgress> getCardWiseProgressMap() {
        return cardWiseProgressMap;
    }

    public void setCardWiseProgressMap(HashMap<Integer, CardProgress> cardWiseProgressMap) {
        this.cardWiseProgressMap = cardWiseProgressMap;
    }

    public boolean isCompleted() {
        return getScore()==SCORE_COMPLETE;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public static int getScoreComplete() {
        return SCORE_COMPLETE;
    }

    public int getIncompleteNewStreak() {
        return incompleteNewStreak;
    }

    public void setIncompleteNewStreak(int incompleteNewStreak) {
        this.incompleteNewStreak = incompleteNewStreak;
    }

    public void incrementIncompleteNewStreak()
    {
        setIncompleteNewStreak(getIncompleteNewStreak()+1);
    }

    @Override
    public String toString() {
        return GsonUtils.getGson().toJson(this).toString(); //To change body of generated methods, choose Tools | Templates.
    }

    public int getScore()
    {
        int score=0;
        for(CardProgress cardProgress: getCardWiseProgressMap().values())
        {
            if(cardProgress.isCorrect())
            {
                score+=1;
            }
        }
        return score;
    }
}
