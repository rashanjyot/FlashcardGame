package com.rashanjyot.flashcardgame.Model;


import com.rashanjyot.flashcardgame.Gen.GsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Deck implements Serializable {


    private Integer deckId;
    private String name;
    private HashMap<Integer,Card> cardMap;

    public Integer getDeckId() {
        return deckId;
    }

    public void setDeckId(Integer deckId) {
        this.deckId = deckId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<Integer, Card> getCardMap() {
        return cardMap;
    }

    public void setCardMap(HashMap<Integer, Card> cardMap) {
        this.cardMap = cardMap;
    }

    @Override
    public String toString() {
        return GsonUtils.getGson().toJson(this).toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
