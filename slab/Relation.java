package com.slab;

import java.io.Serializable;

/**
 * @author ccann
 */
public class Relation implements Serializable{
    private String connective;
    private Entity subject;
    private Entity object;



    public Relation(String connective, Entity subject, Entity object){
        this.connective = connective;
        this.subject = subject;
        this.object = object;
    }

    public Entity getSubject(){
        return this.subject;
    }

    public Entity getObject(){
        return this.object;
    }

    public String getConnective(){
        return this.connective;
    }

    public boolean equals(Relation r){
        return (this.object.getName().equalsIgnoreCase(r.getObject().getName()) &&
                this.subject.getName().equalsIgnoreCase(r.getSubject().getName()) &&
                this.connective.equalsIgnoreCase(r.getConnective()));
    }

}
