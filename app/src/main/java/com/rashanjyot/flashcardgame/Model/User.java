package com.rashanjyot.flashcardgame.Model;

import com.rashanjyot.flashcardgame.Gen.GsonUtils;

import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.types.ObjectId;

public class User implements Cloneable {

    private ObjectId _id;
    private String username;
    private String password;
    private UserProgress userProgress;
    private boolean isTempUser=true;

    public boolean isTempUser() {
        return isTempUser;
    }

    public void setTempUser(boolean tempUser) {
        isTempUser = tempUser;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserProgress getUserProgress() {
        return userProgress;
    }

    public void setUserProgress(UserProgress userProgress) {
        this.userProgress = userProgress;
    }

    @Override
    public String toString() {
        return GsonUtils.getGson().toJson(this).toString(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
