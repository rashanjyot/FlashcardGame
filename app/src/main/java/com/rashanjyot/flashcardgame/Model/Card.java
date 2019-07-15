package com.rashanjyot.flashcardgame.Model;

import com.rashanjyot.flashcardgame.Gen.GsonUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class Card implements Serializable {

    private Integer cardId;
    private Integer type;
    private String ques;
    private ArrayList<Option> optionList;

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getQues() {
        return ques;
    }

    public void setQues(String ques) {
        this.ques = ques;
    }

    public ArrayList<Option> getOptionList() {
        return optionList;
    }

    public void setOptionList(ArrayList<Option> optionList) {
        this.optionList = optionList;
    }

    public ArrayList<Integer> getAnswerIndexList()
    {
        ArrayList<Integer> indexList= new ArrayList<>();
        for(int i=0; i<getOptionList().size(); i++)
        {
            if(getOptionList().get(i).getCorrect())
            {
                indexList.add(i);
            }
        }
        return indexList;
    }

    @Override
    public String toString() {
        return GsonUtils.getGson().toJson(this).toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
