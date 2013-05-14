package com.slab;

import java.util.ArrayList;
import java.io.Serializable;

/**
 * @author: ccann
 *
 * Cans are "canned" utterances that include the sentence, it's semantics, and associated entities.
 */
public class Can implements Serializable{
    public enum UtteranceType {
        QUESTIONWH, STATEMENT,
        QUESTIONYN, GOAL, INTERRUPT, CONFIRMATION, NONSENSE
    }

    private ArrayList<Entity> entities;
    private UtteranceType type;
    private ArrayList<Relation> semantics;
    private String utterance;

    public Can(String s){
        this.utterance = s;
        this.entities = new ArrayList<Entity>();
        this.semantics = new ArrayList<Relation>();
    }

    public ArrayList<Entity> getEntities(){
        return this.entities;
    }

    public void addEntity(Entity e){
        this.entities.add(e);
    }

    public void setType(UtteranceType t){
        this.type = t;
    }

    public UtteranceType getType(){
        return this.type;
    }

    public void addSemantics(Relation a){
        this.semantics.add(a);
    }

    public ArrayList<Relation> getSemantics(){
        return this.semantics;
    }
    public String getUtterance(){
        return this.utterance;
    }

}
