package com.rashanjyot.flashcardgame.Model;

import android.text.TextUtils;


import com.rashanjyot.flashcardgame.Exception.IncompleteDataException;

import java.util.regex.Pattern;


public class RegisterRequest {

    private String  username, password, confirmPassword;

    private RegisterRequest(String username, String password, String confirmPassword) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public static RegisterRequest getInstance(String username, String password, String confirmPassword) throws IncompleteDataException
    {
       if(username==null  || username.trim().isEmpty())
        {
            throw new IncompleteDataException("username");
        }

        else if(password==null  || password.isEmpty())
        {
            throw new IncompleteDataException("password");
        }
        else if(confirmPassword==null || confirmPassword.isEmpty())
        {
            throw new IncompleteDataException("confirm password");
        }
        else if(!password.equals(confirmPassword))
        {
            throw new IncompleteDataException("matching passwords");
        }

        return new RegisterRequest(username, password, confirmPassword);
    }

    public static boolean isValidEmail(String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }



    public String getUsername() {
        return username;
    }



    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }



}
