package com.slab.nlp;

import ade.ADEComponentImpl;

import java.rmi.RemoteException;
import java.util.*;

import com.slab.Can;
import com.slab.Entity;
import com.slab.Relation;

/**
 * @author: ccann
 */
public class CanningComponentImpl extends ADEComponentImpl implements CanningComponent {

    private Map<Class, Object> componentRefs;
    private boolean initialized = false;
    private String canning = "[CanningComponent]: ";
    private String sentence = "";

    public CanningComponentImpl() throws RemoteException {
        super();
        init();
    }

    public boolean addUtterance(String utt){ return true; }

    /**
     * This methods is called by the speech component
     * @param incoming  the sentence-to-date
     * @return true TODO: fix this??
     */
    public boolean addWords(ArrayList<String> incoming){
        if (incoming.contains("0")){
            try{
                for (String s : incoming) {
                    if (!(s.equalsIgnoreCase("0"))) this.sentence += s + " ";
                }
                if (sentence != null) {
                    Can can = generateSemantics(sentence);
                    call(getRefDIDO(),"openCan", can);
                    System.out.println(canning + "sent to DIDOComponent: " + this.sentence);
                    this.sentence = "";
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * generates the semantics for the input sentence, including the creation of new entities in the context.
     * @param sent  the input sentence
     * @return the semantics packaged as an Can type
     */
    public Can generateSemantics (String sent){
        Can can = new Can(sent);

        if (sent.contains("the room in front of you is called the breakroom")){
            can.setType(Can.UtteranceType.STATEMENT);
            Entity breakroom = new Entity("breakroom");
            breakroom.addProperty("location", "in front of you");
            breakroom.addProperty("is a", "room");
            can.addEntity(breakroom);
        }
        else if (sent.contains("is the blue box in the breakroom?")){
            can.setType(Can.UtteranceType.QUESTIONYN);
            Entity blueBox = new Entity("blue box");
            blueBox.addProperty("color", "blue");
            Entity breakroom = new Entity("breakroom");
            Relation r = new Relation("location", blueBox, breakroom);
            can.addSemantics(r);
            //can.addEntity(blueBox);
            //can.addEntity(breakroom);
        }
        else if (sent.contains("is a green box in the breakroom?")){
            can.setType(Can.UtteranceType.QUESTIONYN);
            Entity greenBox = new Entity("green box");
            greenBox.addProperty("color", "green");
            greenBox.addProperty("is a", "box");
            Entity breakroom = new Entity("breakroom");
            Relation r = new Relation("location", greenBox, breakroom);
            can.addSemantics(r);
        }
        else if (sent.contains("what is in the breakroom")){
            can.setType(Can.UtteranceType.QUESTIONWH);
            Entity breakroom = new Entity("breakroom");
            Relation r = new Relation("contains", breakroom, new Entity("null"));
            can.addSemantics(r);
        }
        else if (sent.contains("who is in the breakroom")){
            can.setType(Can.UtteranceType.QUESTIONWH);
            Entity breakroom = new Entity("breakroom");
            Relation r = new Relation("contains", breakroom, new Entity("null"));
            can.addSemantics(r);
        }
        else if (sent.contains("what is the breakroom")){
            can.setType(Can.UtteranceType.QUESTIONWH);
            Entity breakroom = new Entity("breakroom");
            Relation r = new Relation("is a", breakroom, new Entity("null"));
            can.addSemantics(r);
        }
        else if (sent.contains("what is the blue box")){
            can.setType(Can.UtteranceType.QUESTIONWH);
            Entity blueBox = new Entity("blue box");
            blueBox.addProperty("is a", "box");
            blueBox.addProperty("color", "blue");
            Relation r = new Relation("is a", blueBox, new Entity("null"));
            can.addSemantics(r);
        }
        else if (sent.contains("what is the green box")){
            can.setType(Can.UtteranceType.QUESTIONWH);
            Entity greenBox = new Entity("green box");
            greenBox.addProperty("is a", "box");
            greenBox.addProperty("color", "green");
            Relation r = new Relation("is a", greenBox, new Entity("null"));
            can.addSemantics(r);
        }
        else if (sent.contains("there is a green box in the breakroom")){
            can.setType(Can.UtteranceType.STATEMENT);
            Entity greenBox = new Entity("green box");
            greenBox.addProperty("is a", "box");
            greenBox.addProperty("color", "green");
            Entity breakroom = new Entity("breakroom");
            greenBox.addProperty("location", breakroom.getName());
            can.addEntity(greenBox);
            breakroom.addProperty("is a", "room");
            breakroom.addProperty("contains", greenBox.getName());
            can.addEntity(breakroom);
        }
        else if (sent.contains("there is a blue box in the breakroom")){
            can.setType(Can.UtteranceType.STATEMENT);
            Entity blueBox = new Entity("blue box");
            blueBox.addProperty("is a", "box");
            blueBox.addProperty("color", "blue");
            Entity breakroom = new Entity("breakroom");
            blueBox.addProperty("location", breakroom.getName());
            can.addEntity(blueBox);
            breakroom.addProperty("is a", "room");
            breakroom.addProperty("contains", blueBox.getName());
            can.addEntity(breakroom);
        }
        else if (sent.contains("there is a yellow box in the breakroom")){
            can.setType(Can.UtteranceType.STATEMENT);
            Entity yellowBox = new Entity("yellow box");
            yellowBox.addProperty("is a", "box");
            yellowBox.addProperty("color", "yellow");
            Entity breakroom = new Entity("breakroom");
            yellowBox.addProperty("location", breakroom.getName());
            can.addEntity(yellowBox);
            breakroom.addProperty("is a", "room");
            breakroom.addProperty("contains", yellowBox.getName());
            can.addEntity(breakroom);
        }
        else if (sent.contains("snoop dog is in the breakroom")){
            can.setType(Can.UtteranceType.STATEMENT);
            Entity snoopDog = new Entity("snoop dog");
            snoopDog.addProperty("is a", "person");
            Entity breakroom = new Entity("breakroom");
            snoopDog.addProperty("location", breakroom.getName());
            can.addEntity(snoopDog);
            breakroom.addProperty("contains", snoopDog.getName());
            can.addEntity(breakroom);
        }
        else if (sent.contains("when did i tell you about snoop dog")){
            can.setType(Can.UtteranceType.QUESTIONWH);
            Entity snoopDog = new Entity("snoop dog");
            snoopDog.addProperty("is a", "person");
            Relation r = new Relation("genesis", snoopDog, new Entity("null"));
            can.addSemantics(r);
        }
        else if (sent.contains("where is snoop dog")){
            can.setType(Can.UtteranceType.QUESTIONWH);
            Entity snoopDog = new Entity("snoop dog");
            snoopDog.addProperty("is a", "person");
            can.addEntity(snoopDog);
            Relation r = new Relation("location", snoopDog, new Entity("null"));
            can.addSemantics(r);
        }
        else if (sent.contains("our task is to find the blue box")){
            can.setType(Can.UtteranceType.GOAL);
            Entity blueBox = new Entity("blue box");
            blueBox.addProperty("is a", "box");
            blueBox.addProperty("color", "blue");
            Relation r = new Relation("location", blueBox, new Entity("null"));
            can.addSemantics(r);
            can.addEntity(blueBox);
        }
        else if (sent.contains("ok")){
            can.setType(Can.UtteranceType.CONFIRMATION);
        }
        else if (sent.contains("No, No. that's okay.")){
            can.setType(Can.UtteranceType.INTERRUPT);
        }
        else if (sent.contains("yes")){
            can.setType(Can.UtteranceType.CONFIRMATION);
        }

        else {
            System.out.println(canning + "ERROR: don't have semantics for that utterance\n" +
                    "got this sentence: " + sentence);
            can.setType(Can.UtteranceType.NONSENSE);
        }
        return can;
    }


    /**
    * connenct to all components in componentRefs
    */
    private void connectToAllComponents(){
        for(Class c: componentRefs.keySet()) {
            if(componentRefs.get(c) == null) {
                connectTo(c.getName());
            }
        }
    }

    /**
     * connect to the named component
     * @param component the component to connect to
     * @return the reference to the component
     */
    private Object connectTo(String component) {

        Object res = null;
        while (res == null) {
            System.out.println(canning + "Attempting to connect to " + component);
            res = getClient(component);
            if (res == null){
                try {
                    Thread.sleep(2000);
                    System.out.println(canning + "sleeping on connection attempt ...");
                } catch(Exception e) {
                    System.out.println(canning + "ERROR: Can't sleep!");
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Connection established with " + getRefID(res));
        return res;
    }

    /**
     * initialization -- establishes connections to all components
     */
    private void init(){
        if (initialized) return;
        componentRefs = new HashMap<Class, Object>();

        try{
            componentRefs.put(Class.forName("com.slab.dido.DIDOComponent"), null);
        }
        catch(ClassNotFoundException e){
            System.out.println(canning + "ERROR: class not found");
            e.printStackTrace();
        }
        connectToAllComponents();
        initialized=true;
    }

    protected Object getRefDIDO(){
        try {
            return componentRefs.get(Class.forName("com.slab.dido.DIDOComponent"));
        }
        catch(Exception e) {
            System.out.println(canning + "Could not get reference to DIDO component");
            return null;
        }
    }


    
    @Override
    public void onsetDetected(){}

    @Override
    public void offsetDetected(){}

    @Override
    public void updateFromLog(String s){}
    public void updateComponent() { }

    @Override
    public boolean localServicesReady() {return true; }
    @Override
    public String additionalUsageInfo() {return null;}
    @Override
    public boolean parseadditionalargs(String[] args) { return true;}
    @Override
    public void localshutdown() { /* do nothing */ }

    @Override
    public boolean clientDownReact(String p1) {  return true;  }
    @Override
    public void componentDownReact(String componentKey, String[][] constraints) {  }

    @Override
    public boolean localrequestShutdown(Object credentials) { return false; }
    public boolean setLoggingLevel(String levelName) throws RemoteException { return true; }
    public boolean setDevMode(boolean value) throws RemoteException{ return true; }
    public boolean getDevMode() throws RemoteException {return true; }
    public void addTopdownWordBiases(List<String> wordNames, List<Float> relBiases) throws RemoteException{}
    public void cancelAllTopdownWordBiases() throws RemoteException{}
    public void setActor(String name) throws RemoteException {}
    public void setInteractor(String name) throws RemoteException {}
    @Override
    public void notifyComponentJoined(final String newserverkey) {
        new Thread() {
            @Override
            public void run() {
                try{
                    Class joiningComponent = Class.forName(getTypeFromID(newserverkey));
                    for(Class c: componentRefs.keySet())
                        if(c.isAssignableFrom(joiningComponent) && componentRefs.get(c)==null)
                            connectTo(getTypeFromID(newserverkey));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
    @Override
    protected void clientConnectReact(String user) {
        System.out.println(canning + "connected to " + user);
    }
    @Override
    protected void componentConnectReact(String serverkey, Object ref, String[][] constraints) {
        try {
            String newserverkey = getTypeFromID(serverkey);
            System.out.println("NEW SERVER KEY: "+newserverkey);
            Class joiningComponent = Class.forName(newserverkey);
            for(Class c: componentRefs.keySet())
                if(c.isAssignableFrom(joiningComponent)){
                    if(componentRefs.get(c.getClass())==null){
                        System.out.println(canning +" connected to requested component "+joiningComponent);
                        componentRefs.put(c, ref);
                        break;
                    }
                }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}