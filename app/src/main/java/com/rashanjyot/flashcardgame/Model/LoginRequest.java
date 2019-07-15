package com.rashanjyot.flashcardgame.Model;


import com.rashanjyot.flashcardgame.Exception.IncompleteDataException;

public class LoginRequest {

    private String id;
    private String password;

    private LoginRequest(String username, String password)
    {
        this.id=username;
        this.password=password;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public static LoginRequest getInstance(String username, String password) throws IncompleteDataException
    {
        if(username==null  || username.trim().isEmpty())
        {
            throw new IncompleteDataException("username");
        }
        else if(password==null  || password.isEmpty())
        {
            throw new IncompleteDataException("password");
        }

        return new LoginRequest(username,password);
    }
}
