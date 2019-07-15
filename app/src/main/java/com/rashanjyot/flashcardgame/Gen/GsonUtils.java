package com.rashanjyot.flashcardgame.Gen;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.bson.types.ObjectId;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author rashanjot
 */
public class GsonUtils {


    private static final GsonBuilder gsonBuilder = new GsonBuilder()

            .registerTypeAdapter(ObjectId.class, new JsonSerializer<ObjectId>() {
                @Override
                public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.toHexString());
                }
            })
            .registerTypeAdapter(ObjectId.class, new JsonDeserializer<ObjectId>() {
                @Override
                public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//                    return new ObjectId(json.getAsString());
                    return new ObjectId(json.getAsJsonObject().get("$oid").getAsString());
                }
            })
            .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                @Override
                public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
//                     SimpleDateFormat sdfmt=new SimpleDateFormat("dd/MM/yy");
                    SimpleDateFormat sdfmt=new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                    return new JsonPrimitive(sdfmt.format( src ));
                }
            })
            .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    SimpleDateFormat sdfmt=new SimpleDateFormat("dd/MM/yy");
//                    SimpleDateFormat sdfmt=new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

                    try {
                        return sdfmt.parse( json.getAsString());
                    } catch (ParseException ex) {
                        throw new JsonParseException("date error");
                    }
                }
            });



    private static final Gson gson= gsonBuilder.create();

    public static Gson getGson() {
        return gson;
    }

}

