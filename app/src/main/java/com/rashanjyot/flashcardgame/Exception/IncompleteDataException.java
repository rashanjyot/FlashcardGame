package com.rashanjyot.flashcardgame.Exception;

public class IncompleteDataException extends RuntimeException {

    public IncompleteDataException() {

        super();
    }

    public IncompleteDataException(String s) {
       super(s);
    }
}
