package com.rashanjyot.flashcardgame.Activity;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.internal.Primitives;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.rashanjyot.flashcardgame.Exception.NoAnswerException;
import com.rashanjyot.flashcardgame.GlobalApplication;
import com.rashanjyot.flashcardgame.Model.Card;
import com.rashanjyot.flashcardgame.Model.CardProgress;
import com.rashanjyot.flashcardgame.Model.Deck;
import com.rashanjyot.flashcardgame.Model.DeckProgress;
import com.rashanjyot.flashcardgame.Model.Option;
import com.rashanjyot.flashcardgame.Model.User;
import com.rashanjyot.flashcardgame.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static com.rashanjyot.flashcardgame.Gen.Globals.DECK_INTENT_EXTRA;
import static com.rashanjyot.flashcardgame.GlobalApplication.getCurrentUser;

public class GameActivity extends AppCompatActivity {

    private Deck currrentDeck;
    private Card onScreenCard;
    private CardProgress onScreenCardProgress;
    private static final int INCORRECT_NEW_STREAK_THRESHOLD = 5;

    private Random random;
    private static int prevCardId=-1;
    private LinearLayout questionLinLayout;
    private FrameLayout answerFrame;
    private FloatingActionButton submitButton;

    private static final int RADIO_QUESTION=0;
    private static final int CHECKBOX_QUESTION=1;
    private static final int TEXT_QUESTION=2;

    private static final int ANSWER_DISPLAY_TIME=1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        init();
        showCard();
    }

    public void init()
    {
        random= new Random();

        currrentDeck = (Deck) getIntent().getSerializableExtra(DECK_INTENT_EXTRA);

        questionLinLayout= (LinearLayout)findViewById(R.id.questionLinLayout);
        answerFrame= (FrameLayout) findViewById(R.id.answerFrame);

        submitButton=(FloatingActionButton)findViewById(R.id.submitAnswerButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitButtonClicked();
            }
        });
    }

    public void showCard()
    {
        if(getCurrentUser().getUserProgress().getDeckWiseProgressMap().get(currrentDeck.getDeckId()).isCompleted())
        {
            Toast.makeText(getApplicationContext(),"Completed game",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        onScreenCardProgress=findCompetingCard();
        onScreenCard=currrentDeck.getCardMap().get(onScreenCardProgress.getCardId());
        bindCardToScreen();
    }

    public CardProgress findCompetingCard()
    {
        CardProgress selectedCardProgress=null;
        DeckProgress deckProgress=getCurrentUser().getUserProgress().getDeckWiseProgressMap().get(currrentDeck.getDeckId());
        ArrayList<CardProgress> competingCardProgressList= new ArrayList<>();

        deckProgress.incrementIncompleteNewStreak();
        if(deckProgress.getIncompleteNewStreak() >= INCORRECT_NEW_STREAK_THRESHOLD)
        {
            deckProgress.setIncompleteNewStreak(0);
            for(CardProgress cardProgress: deckProgress.getCardWiseProgressMap().values())
            {
                if(cardProgress.isCorrect())
                {
                    competingCardProgressList.add(cardProgress);
                }
            }

        }
        else
        {
            for(CardProgress cardProgress: deckProgress.getCardWiseProgressMap().values())
            {
                if(!cardProgress.isCorrect())
                {
                    switch (cardProgress.getStatus())
                    {
                        case 1: //learning
                        case 2: //reviewing
                        case 3: //checking
                            if(cardProgress.getSchedulingList().get(cardProgress.getStatus()-1)==0)//if zero then add to list
                            {
                                competingCardProgressList.add(cardProgress);
                            }
                            updateSchedulingList(cardProgress);
                            break;
                        case 0: //for new question (unplayed)
                            competingCardProgressList.add(cardProgress);
                            break;
                    }

                }
            }

        }
        if(competingCardProgressList.isEmpty())
        {
            return findCompetingCard();
        }
        else
        {
            selectedCardProgress= competingCardProgressList.get(random.nextInt(competingCardProgressList.size()));
            if (selectedCardProgress.getCardId()==prevCardId)
            {
                selectedCardProgress= findCompetingCard();
            }
        }


        prevCardId=selectedCardProgress.getCardId();
        return selectedCardProgress;

    }

    public void updateSchedulingList(CardProgress cardProgress)
    {
        if(cardProgress.getSchedulingList().get(cardProgress.getStatus()-1)>0)
        {
            cardProgress.getSchedulingList().set(cardProgress.getStatus()-1,cardProgress.getSchedulingList().get(cardProgress.getStatus()-1)-1);
        }
    }

    public void bindCardToScreen()
    {
        String str=null;
        switch (onScreenCardProgress.getStatus())
        {
            case 0: str="New Question"; break;
            case 1: str="Learning"; break;
            case 2: str="Reviewing"; break;
            case 3: str="Checking"; break;
            case 4: str="Revision"; break;
        }
        ((TextView)findViewById(R.id.quesType)).setText(str);
        ((TextView)findViewById(R.id.question)).setText(onScreenCard.getQues());
        answerFrame.removeAllViews();

        int layoutRes=-1;
        switch (onScreenCard.getType())
        {
            case RADIO_QUESTION:
                layoutRes=R.layout.radio_card_layout;
                break;
            case CHECKBOX_QUESTION:
                layoutRes=R.layout.multiselect_card_layout;
                break;
            case TEXT_QUESTION:
                layoutRes=R.layout.text_card_layout;
                break;

        }
        View view= LayoutInflater.from(getApplicationContext()).inflate(layoutRes,answerFrame,true);
        switch (onScreenCard.getType())
        {
            case RADIO_QUESTION:
            case CHECKBOX_QUESTION:
                Option option;
                for (int i=0; i<onScreenCard.getOptionList().size(); i++)
                {
                    ((Button)(view.findViewWithTag(String.valueOf(i)))).setText(onScreenCard.getOptionList().get(i).getVal());
                }
                break;
        }
    }

    public void submitButtonClicked()
    {
        try
        {
            evalUserResponse();

        }
        catch (NoAnswerException e)
        {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Some error occured",Toast.LENGTH_SHORT).show();
        }

    }

    public void evalUserResponse() throws RuntimeException
    {
        if(onScreenCard.getType()==RADIO_QUESTION)
        {
            RadioGroup radioGroup=((RadioGroup)findViewById(R.id.radio_typegroup));
            switch (((RadioGroup)findViewById(R.id.radio_typegroup)).getCheckedRadioButtonId())
            {
                case R.id.radio1:
                case R.id.radio2:
                case R.id.radio3:
                case R.id.radio4:
                    if(onScreenCard.getOptionList().get(Integer.parseInt(findViewById(radioGroup.getCheckedRadioButtonId()).getTag().toString())).getCorrect())
                    {
                        onScreenCardProgress.answeredCorrectly();
                    }
                    else
                    {
                        onScreenCardProgress.answeredIncorrectly();
                    }
                    radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()).setBackgroundColor(getResources().getColor(R.color.red));
                    radioGroup.findViewWithTag(onScreenCard.getAnswerIndexList().get(0).toString()).setBackgroundColor(getResources().getColor(R.color.green));
                    submitButton.hide();
                    break;
                    default:
                        throw new NoAnswerException("Please select an answer!");
            }
        }
        else if(onScreenCard.getType()==CHECKBOX_QUESTION)
        {
            LinearLayout linearLayout=((LinearLayout)findViewById(R.id.checkbox_typegroup));
            ArrayList<Option> optionList=onScreenCard.getOptionList();
            Option option;
            CheckBox checkBox;
            ArrayList<Integer> checkedIndexList= new ArrayList<>();
            for (int i=0; i<optionList.size(); i++)
            {
                if(((CheckBox)linearLayout.findViewWithTag(String.valueOf(i))).isChecked())
                {
                    checkedIndexList.add(i);
                }
            }

            if(checkedIndexList.isEmpty())
            {
                throw new NoAnswerException("Pleast choose atleast one checkbox!");
            }
            submitButton.hide();
            for (int i=0; i<optionList.size(); i++)
            {
                checkBox=((CheckBox)linearLayout.findViewWithTag(String.valueOf(i)));
                option=optionList.get(i);
                if(checkBox.isChecked())
                {

                    if(option.getCorrect())
                    {
                        //green
                        checkBox.setBackgroundColor(getResources().getColor(R.color.green));
                    }
                    else
                    {
                        //red
                        checkBox.setBackgroundColor(getResources().getColor(R.color.red));
                    }
                }
                else
                {
                    if(option.getCorrect())
                    {
                        checkBox.setBackgroundResource(R.drawable.blinking);
                        ((AnimationDrawable) checkBox.getBackground()).start();
                    }
                }
            }

            if(haveSameElements(checkedIndexList,onScreenCard.getAnswerIndexList()))
            {
                //overall correct answer
                onScreenCardProgress.answeredCorrectly();
            }
            else
            {
                //overall incorrect answer
                onScreenCardProgress.answeredIncorrectly();

            }

        }
        else if(onScreenCard.getType()==TEXT_QUESTION)
        {
            String text=((EditText)findViewById(R.id.ansEditText)).getText().toString().trim();
            if(text.isEmpty())
            {
                throw new NoAnswerException("Please type an answer!");
            }
            if(text.equalsIgnoreCase(onScreenCard.getOptionList().get(0).getVal()))
            {
                onScreenCardProgress.answeredCorrectly();
                findViewById(R.id.ansEditText).setBackgroundColor(getResources().getColor(R.color.green));
            }
            else
            {
                onScreenCardProgress.answeredIncorrectly();
                ((TextView)findViewById(R.id.correctAnswer)).setText(onScreenCard.getOptionList().get(0).getVal());
                findViewById(R.id.correctAnswer).setBackgroundColor(getResources().getColor(R.color.green));
                findViewById(R.id.ansEditText).setBackgroundColor(getResources().getColor(R.color.red));
            }
            submitButton.hide();
        }
        ((GlobalApplication)getApplication()).saveCurrentUserProgress();
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        showCard();
                        submitButton.show();
                    }
                },
                ANSWER_DISPLAY_TIME);
    }

    public boolean haveSameElements(ArrayList<Integer> firstList,ArrayList<Integer> secondList)
    {
        if (firstList.size() != secondList.size())
        {
            return false;
        }

        outerloop: for (int i = 0; i < firstList.size(); i++)
        {
            for (int j = 0; j < secondList.size(); j++)
            {
                if(firstList.get(i)==secondList.get(j))
                    continue outerloop;
            }
            return false;
        }

        return true;
    }
}
