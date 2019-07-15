package com.rashanjyot.flashcardgame.Exception;


public class NoAnswerException extends RuntimeException
{
    public NoAnswerException(String s)
    {
        // Call constructor of parent Exception
        super(s);
    }
}