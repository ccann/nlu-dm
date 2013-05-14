package com.slab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.Serializable;

/**
 * @author: ccann
 */
public class Entity implements Serializable{

    private String uniqueID;
    private String name;
    private HashMap<String,ArrayList<String>> properties;
    Random generator;

    public Entity(String name){
        generator = new Random(System.currentTimeMillis());
        this.name = name;
        this.uniqueID = "entity" + Integer.toString(generator.nextInt());
        this.properties = new HashMap<String, ArrayList<String>>();
    }

    public String getUniqueID(){
        return this.uniqueID;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String n){
        this.name = n;
    }

    public void addProperty(String type, String value){
        ArrayList<String> values = new ArrayList<String>();
        values.add(value.toLowerCase());
        addProperty(type, values);
    }

    public void addProperty(String type, ArrayList<String> values){
        if (this.properties.containsKey(type) && !type.equalsIgnoreCase("genesis")){
           // System.out.println("Entity: " + name + " --> overwriting property [" + type + "] with " + values);
        }
        this.properties.put(type, values);
    }

    public ArrayList<String> getValues(String k){
        return this.properties.get(k);
    }

    public HashMap<String,ArrayList<String>> getProperties(){
        return this.properties;
    }
}
