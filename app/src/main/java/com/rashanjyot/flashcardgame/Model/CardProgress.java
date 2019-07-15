package com.rashanjyot.flashcardgame.Model;

import com.rashanjyot.flashcardgame.Gen.GsonUtils;

import java.util.ArrayList;
import java.util.Random;

public class CardProgress {

    public static final int STATUS_NEW=0;
    public static final int STATUS_LEARNING=1;
    public static final int STATUS_REVIEWING=2;
    public static final int STATUS_CHECKING=3;
    public static final int STATUS_CORRECT=4;


    private static final Random random=new Random();
    private Integer cardId;
    private Integer status=STATUS_NEW; // 0 for new, 1  learning, 2 reviewing, 3 checking, 4 correct
    private ArrayList<Integer> schedulingList;


    CardProgress()
    {
        schedulingList= new ArrayList();
        schedulingList.add(0);
        schedulingList.add(0);
        schedulingList.add(0);
    }

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public ArrayList<Integer> getSchedulingList() {
        return schedulingList;
    }

    public void setSchedulingList(ArrayList<Integer> schedulingList) {
        this.schedulingList = schedulingList;
    }

    public boolean isCorrect()
    {
        return status==STATUS_CORRECT;
    }

    public boolean isNew(){ return status==STATUS_NEW; }

    public boolean isIncorrect(){ return status==STATUS_LEARNING || status==STATUS_REVIEWING || status==STATUS_CHECKING ; }

    public void incrementStatus()
    {
        setStatus(getStatus()+1);
    }

    public void setIncorrectStatus()
    {
        setStatus(STATUS_LEARNING);
    }

    public void answeredCorrectly()
    {
      if(getStatus()==STATUS_NEW || getStatus()==STATUS_CORRECT)
      {
          setStatus(STATUS_CORRECT);
      }
      else
      {
          setStatus(getStatus()+1);
      }
    }

    public void answeredIncorrectly()
    {
        setStatus(STATUS_LEARNING);
        generateSchedulingList();
    }

    private void generateSchedulingList()
    {
        //chosen arbbitrary random intervals for spaced repitition [ (1-2), (1-2), (2-3) ]
        getSchedulingList().set(0,random.nextInt(2)+1);
        getSchedulingList().set(1,random.nextInt(2)+1);
        getSchedulingList().set(2,random.nextInt(2)+2);
    }

    @Override
    public String toString() {
        return GsonUtils.getGson().toJson(this).toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
