package com.rashanjyot.flashcardgame.Model;

import com.rashanjyot.flashcardgame.Gen.GsonUtils;

import java.io.Serializable;

public class Option implements Serializable {

    private String val;
    private Boolean isCorrect;


    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    @Override
    public String toString() {
        return GsonUtils.getGson().toJson(this).toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
