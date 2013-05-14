package com.slab.dido;

import ade.ADEComponentImpl;
import java.rmi.RemoteException;
import java.util.*;
import com.slab.Relation;
import com.slab.Entity;
import com.slab.Can;
import com.ADEPercept;

/**
 * @author: ccann
 * Dynamic Initiative Dialogue Operator
 */
public class DIDOComponentImpl extends ADEComponentImpl implements DIDOComponent {
    private Map<Class, Object> componentRefs;
    private String tts = "com.tts.MaryTTSComponent";
    private String motion = "com.motion.MotionComponent";
    private String pioneer = "com.adesim.SimPioneerComponent";

    private String dido = "[DIDO]: ";                     // for identifying the component output in terminal
    private String currLocationID = "hallway";            // emulating some SPEX functionality

    private static final double INITIATIVE_THRESHOLD = 10.00;

    private Random generator;
    private ArrayList<String> verbs;                      // list of entity properties that are verbs
    private ArrayList<Entity> beliefCloud;                // composed of "believed-in" entities
    private LinkedList<Relation> actionQueue;             // actions to be executed upon update
    private LinkedList<Can> vendingMachine;               // stores and vends semantic cans from CanningComponent
    private LinkedList<String> utteranceQueue;            // stores utterances to be said
    private ArrayList<Relation> goals;                    // stores goals in Relation form
    private ArrayList<ADEPercept> percepts;               // stores current percepts in ADEPercept form
    private ArrayList<Relation> potentialGoals;           // stores potential goals (based on utterances from interloc)
    private HashMap<String, String> associations;         // stores property associates for belief inferences
    private ArrayList<String> reportedPerceptNames;        // entities that the robot has reported to the interlocutor
    private double[] breakroomLoc = {-12.0, -0.5};
    private HashMap<String, double[]> knownLocations;

    private boolean initialized = false;                  // is component ready?
    private boolean beliefsInitialized = false;           // using extreme caution here with ensuring beliefs are ready
    private boolean waitingForConfirmation = false;       // waiting for confirmation from interloc of potential goal?

    /**
     * Constructor -- construct new DIDO Component
     * @throws RemoteException
     */
    public DIDOComponentImpl() throws RemoteException{
        super();
        init();
        initBeliefs();
        initPOS();
        initGoals();
        initAssociations();
        initKnownLocations();

        actionQueue = new LinkedList<Relation>();
        vendingMachine = new LinkedList<Can>();
        utteranceQueue = new LinkedList<String>();
        generator = new Random();
        percepts = new ArrayList<ADEPercept>();
        potentialGoals = new ArrayList<Relation>();
        reportedPerceptNames = new ArrayList<String>();
    }

    /**
     * updateComponent constantly *updates or performs* percepts, utterances, incoming semantics, and actions
     */
    @Override
    public void updateComponent() {
        if(!initialized) return;

        this.percepts = getAllPercepts();
        //checkIfGoalsComplete();
        actOnPercepts();


        if (utteranceQueue.size() > 0){
            speak(utteranceQueue.pop());
        }

        if(vendingMachine.size() > 0) {
            arbitrate();
        }

        if (actionQueue.size() > 0) {
            Relation nextAction = actionQueue.pop();
            if (nextAction != null) {
                execAction(nextAction);
            }
        }
    }

    /**
     * send off the state-appropriate utterance or action to the relevant queue
     */
    private void arbitrate(){
        if (!initialized) return;

        Can can = vendingMachine.pop();
        switch (can.getType()){

            case QUESTIONYN:
                utteranceQueue.add(answerQuestionYN(can));
                checkForNewGoal(can);
                break;
            case QUESTIONWH:
                utteranceQueue.add(answerQuestionWH(can));
                checkForNewGoal(can);
                break;
            case CONFIRMATION:
                utteranceQueue.add(handleConfirmation());
                break;
            case NONSENSE:
                utteranceQueue.add("i do not understand.");
                break;
            default: // STATEMENT, GOAL
                utteranceQueue.add(genUttConfirmation());
                break;
        }
    }

    /**
     * if just asked interlocutor to confirm a new goal, and he says yes, add it
     * Confirm that you understand either way.
     * @return confirmation utterance
     */
    private String handleConfirmation(){
        if (waitingForConfirmation){
            for (Relation potentialGoal : potentialGoals) {
                Can can = new Can("");
                can.setType(Can.UtteranceType.GOAL);
                can.addSemantics(potentialGoal);
                addGoals(can);
                printGoals();
            }
            return genUttConfirmation();
        } return ""; // say nothing at all if interlocutor is just responding "OK" to something you said.
    }

    /**
     * This method gets called by the CanningComponent. It provides a can full of
     * 1) the original utterance                   can.getUtterance()
     * 2) the semantics of that utterance          can.getSemantics()
     * 3) the type of the utterance                can.getType()
     * 4) the entities relevant to the utterance   can.getEntities()
     *
     * @param can  the can - crack it open look at all those fizzy semantics
     * @throws RemoteException
     */
    public void openCan(Can can) throws RemoteException {
        System.out.println(dido + "** cracking open a fresh can of semantics! **");

        // assert these entities into the belief state of the robot, so it "knows" about them
        for (Entity entity : can.getEntities()) {
            entity.addProperty("genesis", Long.toString(System.currentTimeMillis()));
            bubbleUp(entity);
        }

        addGoals(can);
        //printGoals();
        //printBeliefCloud();
        vendingMachine.add(can);
    }

    /**
     * Choose the entity from qualified entities in belief cloud. Perhaps query the interlocutor.
     * @param entities the candidate entities
     * @return the chosen entity
     */
    private Entity disambiguate(ArrayList<Entity> entities){
        return entities.get(0);
    }

    /**
     * TODO: fix this so it works for multiple relations. (&& all of them)
     * returns YES or NO based on relations between the beliefs and the input semantics
     * @param can the canned semantics
     * @return the string representing YES or NO
     */
    private String answerQuestionYN(Can can){
        String wholeAns = "";
        for (Relation r : can.getSemantics()) {
            String ans = "";
            if (concernsAGoal(r)) ans += seekGoal(r);

            // if concernsAGoal returns empty string it's because it can't execAction on that property
            else if (ans.equalsIgnoreCase("")) {
                Entity subject = r.getSubject();
                Entity object = r.getObject();
                String prop = r.getConnective();
                //System.out.println(dido+subject.getName() + "," + object.getName() + "," + prop);
                //System.out.println(dido+"goals: " + goals);

                if(believeIn(subject)) {
                    //System.out.println(dido + "Believe in " + subject.getName());
                    if(believeIn(object)) {
                        //System.out.println(dido + "Believe in " + object.getName());
                        ArrayList<Entity> matchingEntities = getMatchingEntitiesByProperties(subject);
                        Entity matchedBelief = disambiguate(matchingEntities);
                        if(hasProperty(prop, matchedBelief)) {
                          //  System.out.println(dido + "belief has matching property: " + prop);
                            ArrayList<String> beliefValues = replaceIDsWithNames(matchedBelief.getProperties().get(prop));
                            ArrayList<String> targetValues = new ArrayList<String>();
                            targetValues.add(object.getName());
                            //System.out.println(dido + "answer YES if I believe "+subject.getName()+" "+prop+" "+targetValues);
                            if (subsetOf(beliefValues, targetValues)){
                                ans += genUttAffirmative();
                            } else {
                                ans += genUttNegative();
                            }
                        } else {
                            ans += "i do not know that about " + subject.getName();
                        }
                    } else {
                        ans += "i do not know what " + object.getName() + " is";
                    }
                } else{
                    ans += "i do not know about " + subject.getName();
                }
            } wholeAns += ans;
        } return wholeAns;
    }

        /**
         * return the answer to a WH question
     * @param can the canned semantics of the question
     * @return the answer to output in String form
     */
    private String answerQuestionWH(Can can){
        String ans = "";
        for (Relation r : can.getSemantics()) {
            Entity subject = r.getSubject();
            String prop = r.getConnective();
            //System.out.println(dido + "subject: " + subject.getName() + " prop " + prop);
            if (believeIn(subject)) {
                //System.out.println(dido + "TEST: " + "I believe in " + subject.getName());
                ArrayList<Entity> matchingEntities = getMatchingEntitiesByName(subject);
                Entity match = disambiguate(matchingEntities);
                ArrayList<String> values = match.getProperties().get(prop);
               // System.out.println(dido+"values: "+match.getProperties().get(prop));
                if (values == null) {
                    ans += "i do not know";
                } else {
                  //  System.out.println(dido+"with ids replaced: " + replaceIDsWithNames(values));
                    ans += genUttWH(replaceIDsWithNames(values), subject, prop, can.getUtterance());
                }
            } else {
                ans += "i do not know about " + subject.getName();
            }
        }
        return ans;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////      Actions and Goals      //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    private void checkIfGoalsComplete(){
//        for (Relation goal : goals) {
//            if (believeIn(goal.getSubject())){
//                //System.out.println(dido+"checkIfGoalsComplete: believe in " + goal.getSubject().getName());
//                ArrayList<Entity> matches = getMatchingEntitiesByName(goal.getSubject());
//                for (Entity match : matches) {
//                    if (hasProperty(goal.getConnective(), match)){
//                   //     System.out.println(dido+"checkIfGoalsComplete: found matching entity w/ prop. adding utterance");
//                        utteranceQueue.add("Goal Completed. " + goal.getSubject().getName() +
//                                " has " + goal.getConnective() + " " +
//                                interposeAnds(match.getValues(goal.getConnective())));
//                    }
//                }
//            }
//        }
//    }

    /**
     * Call this method during updateComponent.
     * The robot acts on all goal-related percepts in various ways.
     */
    private void actOnPercepts(){
        //System.out.println(dido+"calling actOnPercepts");
        HashMap<Entity,Relation> perceivedGoalEntities = getPerceivedGoalEntities();
        ArrayList<ADEPercept> perceivedGoalRelatedEntities = getGoalRelatedPercepts();

       // System.out.println(dido+"size: " +perceivedGoalRelatedEntities.size());
        if (!perceivedGoalEntities.isEmpty()){
            printPercepts();
            //System.out.println(dido + "goalSubs is not empty");
            String utt = "Goal completed. i have located ";
            ArrayList<String> perceivedGoalEntityNames = new ArrayList<String>();
            for (Entity gs : perceivedGoalEntities.keySet()) {
                perceivedGoalEntityNames.add(gs.getName());
                updateGoals(perceivedGoalEntities.get(gs)); // goal has been accomplished, kick it from belief cloud
            }
            inferNewBeliefProperties();
            utt += interposeAnds(perceivedGoalEntityNames);
            utteranceQueue.add(utt);
        }
        else if (!perceivedGoalRelatedEntities.isEmpty()){
            printPercepts();
            ArrayList<String> goalRelatedPerceptNames = new ArrayList<String>();
            for (ADEPercept p : getGoalRelatedPercepts()) {
                //System.out.println(dido+"have I reported " + p.name + " before?");
                String pName = p.name+p.color+p.type;
                if (!reportedPerceptNames.contains(pName)){
                   // System.out.println(dido+"no I haven't");
                    goalRelatedPerceptNames.add(p.color + " "+p.type);
                    this.reportedPerceptNames.add(pName);
                }
                else{
                   // System.out.println(dido+"yes I have");
                }
            }
            if (!goalRelatedPerceptNames.isEmpty()){
               // System.out.println(dido+"seeing new entities");
                String utt = "i see ";
                inferNewBeliefProperties();
                utt += interposeAnds(goalRelatedPerceptNames);
                utteranceQueue.add(utt);
            }
        }
    }

    /**
     * Executes the actions (speak, navigate, etc.) necessary to *act on* the input relation
     * @param action
     * @return
     */
    private boolean execAction
    (Relation action){
        boolean completed = false;
        Entity actionObject = action.getObject();
        String actionType = action.getConnective();

        if (actionType.equalsIgnoreCase("go to")){
            if (actionObject.getName().equalsIgnoreCase("breakroom") && !currLocationID.equalsIgnoreCase("breakroom")){
                // door right: -12.2  -1.50
                // door left: -13.11 -1.56

                    moveDist(2.00);
                    this.currLocationID = "breakroom";


            }
        } return completed;
    }

    /**
     * Find all goal entities that are currently perceived by the robot
     * e.g. perceiving ADEPercept name: box1 color: blue
     * have goal involving entity type: box  color: blue       -->  add to list
     * @return perceived goal entities in a list
     */
    private HashMap<Entity,Relation> getPerceivedGoalEntities(){
        HashMap<Entity,Relation> ret = new HashMap<Entity, Relation>();
        // System.out.println(dido+percepts + " :: " + goals);
        for (ADEPercept percept : percepts) {
            for (Relation goal : goals) {
                Entity sub = goal.getSubject();
                ArrayList<String> subjectIsAValues = sub.getValues("is a");
                if (!subjectIsAValues.isEmpty()){
                    for (String subjectIsAValue : subjectIsAValues) {
                        if (percept.type.toLowerCase().contains(subjectIsAValue)){
                            ArrayList<String> subColors = sub.getValues("color");
                            if (!subColors.isEmpty()) {
                                for (String subColor : subColors) {
                                    //System.out.println(dido + "sub color: " + subColor + " percept color: " + percept.color);
                                    if(percept.color.equalsIgnoreCase(subColor)){
                                        System.out.println(dido + "goal percept detected: " + sub.getName());
                                        ret.put(sub,goal);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } return ret;
    }

    /**
     * Find all goal RELATED entities that are currently perceived by the robot
     * e.g. perceiving ADEPercept name: box1 color: blue
     * have goal involving entity type: box  color: blue       -->  add to list
     * @return perceived goal entities in a list
     */
    private ArrayList<ADEPercept> getGoalRelatedPercepts(){
        ArrayList<ADEPercept> ret = new ArrayList<ADEPercept>();

        for (ADEPercept percept : percepts) {
            //System.out.println(dido+"percept: " + percept.name);
            for (Relation goal : goals) {
                //System.out.println(dido+"goal: " +goal.getSubject().getName());
                Entity goalSubject = goal.getSubject();
                ArrayList<String> subjectIsAValues = goalSubject.getValues("is a");
                //System.out.println(dido+"are "+goal.getSubject().getName()+ " is_a property values empty?");
                if (!subjectIsAValues.isEmpty()){
                   // System.out.println(dido+"No");
                    for (String subjectIsAValue : subjectIsAValues) {
                    //    System.out.println(dido+"does "+ percept.type.toLowerCase()+" contain "+subjectIsAValue.toLowerCase());
                        if (percept.type.toLowerCase().contains(subjectIsAValue.toLowerCase())){
                      //      System.out.println(dido+"Yes!");
                            //System.out.println(dido + "goal-RELATED percept detected: " + sub.getName());
                            ret.add(percept);
                        }
                    }
                }
            }
        } return ret;
    }

    /**
     * does this relation concern a goal?
     * @param r the relation
     * @return true if the relation concerns a goal, false otherwise
     */
    private boolean concernsAGoal(Relation r){
        //System.out.println(dido + "concerns a goal! " + r);
        for (Relation goal : goals) {
            if( (r.getConnective().equalsIgnoreCase(goal.getConnective())) &&
                    (r.getSubject().getName().equalsIgnoreCase(goal.getSubject().getName()))) {
                //System.out.println(dido + "found concerning goal!");
                return true;
            }
        } return false;
    }

    /**
     * do the necessary action to seek the goal contained in input relation.
     * e.g. Relation r = ("go to", self, breakroom)
     * sends call to motion component to drive the robot to the breakroom.
     * @param r
     * @return
     */
    private String seekGoal(Relation r){
        //Entity subject = r.getSubject();
        Entity object = r.getObject();
        String prop = r.getConnective();

        //System.out.println(dido+"Calling seekGoal on " + object.getName() + " and " + prop);

        // initiate action to check breakroom
        if (prop.equalsIgnoreCase("location")){
            Entity self = disambiguate(getMatchingEntitiesByName(new Entity("self")));

            if(getDistanceToLocation(object.getName()) < INITIATIVE_THRESHOLD){
                //System.out.println(dido + "adding action to queue");
                actionQueue.add(new Relation("go to", self, object));
                return genUttTakingAction();
            }
            else{
                return "I do not know";
            }
        }
        return "";
    }

    /**
     * add the relations in the can semantics to the goals list
     * @param can
     */
    private void addGoals(Can can){
        // getting a new goal assignment, add it to goal list
        if (can.getType() == Can.UtteranceType.GOAL){
            for (Relation relation : can.getSemantics()) {
                goals.add(relation);
            }
        }
    }

    /**
     * check for an entity related to the goal in the question being asked
     * if there is one, ask if we should have a goal to answer this question
     * @param can sems
     */
    private void checkForNewGoal(Can can){
        if (can.getType() == Can.UtteranceType.QUESTIONWH || can.getType() == Can.UtteranceType.QUESTIONYN){

            for (Relation relation : can.getSemantics()) {
                //System.out.println(dido+"considering adding a new goal concerning " + relation.getSubject().getName());
                if (!concernsAGoal(relation)){
                    //System.out.println(dido+"doesn't concern a goal we already have");
                    //System.out.println(dido+"relation: " + relation.getConnective() + " :: " + relation.getSubject().getName() + " :: " + relation.getObject().getName());
                    ArrayList<String> types = relation.getSubject().getValues("is a");
                    //System.out.println(dido+"types: " + types);
                    if (types != null){
                        //System.out.println(dido+"types not null");
                        for (String type : types) {
                            for (Relation goal : goals) {
                                Entity goalEnt = goal.getSubject();
                                for (String goalType : goalEnt.getValues("is a")) {
                                    if(type.equalsIgnoreCase(goalType)){
                                        utteranceQueue.add("Do we have a goal to know " + relation.getConnective() + " of " + relation.getSubject().getName());
                                        this.waitingForConfirmation = true;
                                        Relation newR = new Relation(relation.getConnective(), relation.getSubject(), new Entity("null"));
                                        potentialGoals.add(newR);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////       Component Calls       //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Speak an utterance with TTS
     * @param utt the utterance to speak
     * @return true if used TTS component to speak, false otherwise
     */
    private boolean speak(String utt){
        boolean result;
        try {
            Object ref_tts = getRefTTS();
            call(ref_tts,"sayText", utt);
            result = true;
        } catch (Exception ex){
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * move the specified number of meters forward
     * @param dist double representing number of meters
     * @return true if got reference to motion component and sent method call
     */
    private boolean moveDist(double dist){
        try {
            Object refMotion = getRefMotion();
            call(refMotion, "moveDist", dist);
            return true;
        }
        catch (Exception e) {
            System.out.println("could not make call to MotionComponent");
            e.printStackTrace();
        } return false;
    }

    /**
     * get the list of percepts from the Sim Pioneer component
     * @return list of all percepts
     */
    private ArrayList<ADEPercept> getAllPercepts(){
        ArrayList<ADEPercept> objects = new ArrayList<ADEPercept>();
        try {
            Object refPioneer = getRefPioneer();
            ArrayList<ADEPercept> obs = (ArrayList<ADEPercept>) call(refPioneer, "lookFor", "*");
            objects.addAll(obs);
            createBeliefsAboutPercepts(objects);
        }
        catch (Exception e) {
            System.out.println("could not make call to MotionComponent");
            e.printStackTrace();
        }
        return objects;
    }

    /**
     * get the current location of the robot
     * @return my current location
     */
    private double[] getMyLocation(){
        double[] ret = {};
        try {
            Object refPioneer = getRefPioneer();
            double[] pose = (double[]) call(refPioneer, "getPoseGlobal");
            ret = pose;
        }
        catch (Exception e) {
            System.out.println("could not make call to MotionComponent");
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * get the distance to the input location from current location
     * @param location the target location
     * @return distance from current location to target
     */
    private double getDistanceToLocation(String location){
        double[] loc = this.knownLocations.get(location);
        double[] myloc = getMyLocation();
        double dist = getDistance(myloc[0],myloc[1],loc[0],loc[1]);
        System.out.println(dido+"distance from me to "+location+" is: " + dist);
        return dist;
    }


    public double getDistance(double x, double y, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////     Belief        Functions    ///////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void printBeliefCloud(){
        System.out.println(dido+"              ___BELIEFS___                ");
        for (Entity belief : beliefCloud) {
            System.out.println(dido + belief.getName() + ": " + belief.getProperties());
        }
        System.out.println(dido+"\n");
    }


    private void printGoals(){
        System.out.println(dido+"              ___GOALS___                ");
        for (Relation goal : goals) {
            System.out.println(dido + goal.getConnective() + " :: " + goal.getSubject().getName() + " :: " + goal.getObject().getName());
        }
        System.out.println(dido+"\n");
    }

    private void printPercepts(){
        System.out.println(dido+"              ___PERCEPTS___                ");
        for (ADEPercept percept : percepts) {
            System.out.println(dido+":name " + percept.name + " :is_a " + percept.type + " :color " +percept.color);
        }
        System.out.println(dido+"\n");
    }

    /**
     * turn ADEPercepts in global percept list into belief entities in beliefCloud
     * @param ps ADEPercepts from SimPioneer perception
     */
    private void createBeliefsAboutPercepts(ArrayList<ADEPercept> ps){
        for (ADEPercept p : ps) {
            Entity newPercept = new Entity(p.name);
            if (p.color != null){
                newPercept.addProperty("color", p.color);
            }
            if (p.type != null){
                newPercept.addProperty("is a", p.type);
            }

            newPercept.addProperty("location", currLocationID);
            bubbleUp(newPercept);
        }
    }

    /**
     * Bubble up an entity into the "belief cloud". It is now "believed-in"
     * If entity previously unknown, add it to beliefCloud.
     *
     * @param e  the new entity about which you are asserting a belief
     */
    private void bubbleUp(Entity e){
        while(!beliefsInitialized)
        {
            System.out.println(dido + "... waiting for belief initialization");
            try {
                Thread.sleep(100);
            } catch(Exception ex){
                System.out.println("could not sleep!!!");
                ex.printStackTrace();
            }
        }
        if(believeIn(e)) {
            //System.out.println(dido + "I ALREADY believe in " + e.getName());
            ArrayList<Entity> entities = getMatchingEntitiesByName(e);
            Entity ent = disambiguate(entities);
            //printBeliefCloud();
            //System.out.println(dido + "removing " + e.getName() + " from belief cloud");
            removeEntityFromBeliefs(ent);
            //printBeliefCloud();
            beliefCloud.add(mergeInto(ent, e));
            //printBeliefCloud();
        }
        else {
            System.out.println(dido + "I NOW believe in " + e.getName());
            printBeliefCloud();
            beliefCloud.add(e);
        }
    }

    /**
     * merge ent1 (in the belief cloud) with ent2
     * @param ent1 the entity already in the belief cloud
     * @param ent2 the entity to merge into ent1
     * @return the merge entity
     */
    private Entity mergeInto(Entity ent1, Entity ent2){
        //System.out.println(dido + "MERGING: " + ent1.getName() + " into " + ent2.getName());
        Set<String> ent1Props = ent1.getProperties().keySet();
        Set<String> ent2Props = ent2.getProperties().keySet();

        for (String prop : ent2Props) {
            if (!prop.equalsIgnoreCase("genesis")){
                if(ent1Props.contains(prop)){
                    for (String s : ent2.getValues(prop)) {
                        if (!ent1.getProperties().get(prop).contains(s)){
                            System.out.println(dido + "adding value: " + s + " to " + prop );
                            ent1.getProperties().get(prop).add(s);
                        }
                    }
                }
                else {
                    System.out.println(dido + "adding " + ent2.getValues(prop) + " to " + prop +" of " +ent1.getName());
                    ent1.addProperty(prop, ent2.getValues(prop));
                }
            }
        }
            //System.out.println(dido + "returning: " + ent1.getName() + " with ID: " + ent1.getUniqueID());
            return ent1;
    }

    /**
     * Determine if have believe in entities that have the same name as e
     * @param e the entity
     * @return true if you do, false otherwise
     */
    private boolean believeIn(Entity e){
       return !getMatchingEntitiesByName(e).isEmpty();
    }

    /**
     * find all the entities in belief cloud that match by name input entity
     * @param input the entity
     * @return list of matching entities
     */
    private ArrayList<Entity> getMatchingEntitiesByName(Entity input){
        ArrayList<Entity> ret = new ArrayList<Entity>();
        for (Entity entity : beliefCloud) {
            if (entity.getName().equalsIgnoreCase(input.getName())) {
                ret.add(entity);
            }
        }
        return ret;
    }

    /**
     * get the entities that have the same properties as input
     * @param input
     * @return
     */
    private ArrayList<Entity> getMatchingEntitiesByProperties(Entity input){
        ArrayList<Entity> ret = new ArrayList<Entity>();
        for (Entity entity : beliefCloud) {
            boolean shouldAdd = true;
            for (String property : input.getProperties().keySet()) {
                if (!subsetOf(entity.getValues(property), input.getValues(property))){
                    shouldAdd = false;
                }
            } if(shouldAdd) ret.add(entity);
        } return ret;
    }

    /**
     * remove the input entity from the belief cloud
     * @param e the entity to remove
     */
    private void removeEntityFromBeliefs(Entity e){
        ArrayList<Entity> newBeliefCloud = new ArrayList<Entity>();
        for (Entity entity : beliefCloud) {
            if(!entity.getName().equalsIgnoreCase(e.getName())){
                newBeliefCloud.add(entity);
            }
        }
        beliefCloud = newBeliefCloud;
    }

    /**
     * remove a goal from the goals list
     * @param r the goal relation to remove
     */
    private void updateGoals(Relation r){
        //remove the accomplished goal
        ArrayList<Relation> newGoals = new ArrayList<Relation>();
        for (Relation goal : goals) {
            if (! r.equals(goal)) {
                newGoals.add(goal);
            }
        } this.goals = newGoals;
        printGoals();
        //assert new beliefs based on that accomplishment
        Entity rEntity = new Entity(r.getSubject().getName());
        rEntity.addProperty(r.getConnective(), this.currLocationID);
        bubbleUp(rEntity);
    }

    /**
     * populate belief cloud with new properties based on existing ones
     */
    private void inferNewBeliefProperties(){
        for (Entity entity : beliefCloud) {
            for (String s : associations.keySet()) {
                    if (entity.getProperties().keySet().contains(s)){
                        if (entity.getValues(s) != null){
                            for (String entName : entity.getValues(s)) {
                                ArrayList<Entity> ents = getMatchingEntitiesByName(new Entity(entName));
                                for (Entity ent : ents) {
                                    ent.addProperty(associations.get(s), entity.getName());
                                }
                            }
                        }
                    }
            }
        }
    }

    /**
     * determine if the input entity has a specified property
     * @param prop the property
     * @param e the entity
     * @return true if the entity has the prop, false otherwise
     */
    private boolean hasProperty(String prop, Entity e){
        return e.getProperties().containsKey(prop);
    }

    /**
     * Gets an entity from the belief cloud by it's unique String id
     * @param id the unique ID of the entity
     * @return the Entity
     */
    private Entity getEntityByID(String id){
        //System.out.println(dido+"gets inside getEntityByID for: " + id);
        for (Entity entity : beliefCloud) {
            System.out.println(dido+"trying to match entity by ID with: " + entity.getName() + " "+ entity.getUniqueID());
            if(entity.getUniqueID().equalsIgnoreCase(id)){
                return entity;
            }
        }
        System.out.println(dido+"returning NULL -- could not find ID");
        return null;
    }

    /**
     * replaces all identifiers (e.g. entity7623823) with their names (e.g. green box)
     * @param values the list of values
     * @return the list with all identifiers replaces with names
     */
    private ArrayList<String> replaceIDsWithNames(ArrayList<String> values){
        //System.out.println(dido+"gets inside replace IDS");
        ArrayList<String> ret = new ArrayList<String>();
        if (values == null) return new ArrayList<String>();
        if (values.isEmpty()) return new ArrayList<String>();
        for (String value : values) {
            //System.out.println(dido+"replaceID: value: " + value );
            if (value.contains("entity")){
                System.out.println(dido + "trying to get entity by ID: " + value);
                if(getEntityByID(value) == null){
                    // do not believe in an entity with that ID, try to resolve by name instead

                }
                ret.add(getEntityByID(value).getName());
            }
            else { ret.add(value); }
        }
        return ret;
    }

    /**
     * gets the list of entities which have this value (among others) assigned to this property
     * @param prop the property
     * @param value the value
     * @return the list of entities which have prop-value
     */
    private ArrayList<Entity> getEntitiesWithPropertyValue(String prop, String value){
        ArrayList<Entity> ret = new ArrayList<Entity>();
        for (Entity entity : beliefCloud) {
            if (entity.getProperties().keySet().contains(prop)){
                if (entity.getProperties().get(prop).contains(value)){
                      ret.add(entity);
                }
            }
        }
        return ret;
    }

    /**
     * Check if list2 is a subset of list1.
     * @param list1
     * @param list2
     * @return true if list2 subset of list1, false otherwise
     */
    private boolean subsetOf(ArrayList<String> list1, ArrayList<String> list2){
        list1 = replaceIDsWithNames(list1);
        //System.out.println(dido + "belief List: " + replaceIDsWithNames(list1));
        //System.out.println(dido + "asserted list: " + list2);
        for (String s : list2) {
            if (! list1.contains(s)) return false;
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////     Utterance Generation   ///////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * return one of several utterances (randomly) to indicate confirmation / understanding
     * @return the utterance as a String
     */
    private String genUttConfirmation(){
        String[] sent = {"Okay", "Understood", "Got it", "Alright", "Acknowledged", "Roger roger"};
        int r = generator.nextInt(sent.length);
        return sent[r];
    }

    /**
     * return one of several utterances (randomly) to indicate affirmation
     * @return the utterance as a String
     */
    private String genUttAffirmative(){
        String[] sent = {"Yes. ", "Affirmative. ", "Indubitably. "};
        int r = generator.nextInt(sent.length);
        return sent[r];
    }

    /**
     * generate one of several utterances (randomly) to indicate the negative
     * @return the utterance
     */
    private String genUttNegative(){
        String[] sent = {"No. ", "Negative. "};
        int r = generator.nextInt(sent.length);
        return sent[r];
    }

    /**
     * generate one of several utterance (randomly) to indicate initative-taking
     * @return the utterance
     */
    private String genUttTakingAction(){
        String [] sent = {"Hold on, i will check. ", "Let me check. ", "i do not know, let me check. ", "One minute, I'll check. "};
        int r = generator.nextInt(sent.length);
        return sent[r];
    }

    /**
     * generate a response using contents of the WH question.
     * e.g.  What is in the breakroom?
     *  -->  Breakroom contains green box and blue box
     * @param values     the value of the property in question
     * @param sub       the subject
     * @param prop      the property in question
     * @param question  the question being asked of the robot
     * @return an utterance answering the WH question
     */
    private String genUttWH(ArrayList<String> values, Entity sub, String prop, String question){
        String utt = "";
        String[] qArr = question.split("\\s+");
        String firstWord = qArr[0].toLowerCase();
        //String secondWord = sentArr[1].toLowerCase();

        if (firstWord.contains("what") || firstWord.contains("where")) {
            String valueStr = interposeAnds(values);
            String helper = " ";
            if(!verbs.contains(prop)) helper = " has ";
            utt = sub.getName() + helper + prop + " " + valueStr;
        }
        else if (firstWord.contains("who")) {
            // create subset that is composed of just PERSONS
            ArrayList<String> whos = new ArrayList<String>();
            ArrayList<Entity> entities = getEntitiesWithPropertyValue("is a", "person");
            for (Entity entity : entities) {
                if (values.contains(entity.getName())) whos.add(entity.getName());
            }
            if (whos.isEmpty()){
                System.out.println(dido+"ERROR no entities have those names");
                return "nobody";
            }
            String valueStr = interposeAnds(whos);
            String helper = " ";
            if(!verbs.contains(prop)) helper = " has ";
            utt = sub.getName() + helper + prop + " " + valueStr;
        }
        else if(firstWord.contains("when")){
            long valueGenesis = Long.valueOf(values.get(0)).longValue();
            //System.out.println(dido+"value genesis: " + values.get(0));
            long timeSince = System.currentTimeMillis() - valueGenesis;
            //System.out.println(dido+"current time: " + System.currentTimeMillis());
            //System.out.println(dido+"timeSince: "+ Long.toString(timeSince));
            utt = (double)timeSince/1000.00 + " seconds ago.";
        }

        return utt;
    }

    /**
     * e.g. ["green box", "blue box"] --> "green box and blue box"
     * @param s the list of strings to append and interpose with Ands
     * @return the new String with Ands between each element of S
     */
    private String interposeAnds(ArrayList<String> s){
        String valueStr = "";
        for (int i = 0; i < s.size(); i++){
            if ((i != 0) && (i == s.size()-1 )){
                valueStr += " and " + s.get(i);
            }
            else if (i > 0) {
                valueStr += ", " + s.get(i);
            }
            else { valueStr += s.get(i); }
        }
        return valueStr;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////     Initializers     /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * initialization -- establishes connections to all components
     */
    private void init(){
        if (initialized) return;

        componentRefs = new HashMap<Class, Object>();

        try{
            componentRefs.put(Class.forName(motion), null);
            componentRefs.put(Class.forName(tts),null);
            componentRefs.put(Class.forName(pioneer), null);
        }
        catch(ClassNotFoundException e){
            System.out.println(dido + "ERROR: class not found");
            e.printStackTrace();
        }

        connectToAllComponents();
        initialized=true;
    }

    /**
     * initialize the set of pre-established beliefs
     */
    private void initBeliefs(){
        beliefCloud = new ArrayList<Entity>();

        Entity e = new Entity("self");
        System.out.println(dido + "I now believe in " + e.getName());
        e.addProperty("genesis", Long.toString(System.currentTimeMillis()));
        e.addProperty("location", currLocationID);
        beliefCloud.add(e);

        Entity greenBox = new Entity("green box");
        e.addProperty("is a", "box");
        beliefCloud.add(greenBox);
        beliefsInitialized = true;
    }

    /**
     * Initalize which properties are, e.g., verbs
     * This is only used for producing more natural utterances
     */
    private void initPOS(){
        verbs = new ArrayList<String>();
        verbs.add("contains");
        verbs.add("is a");
    }

    /**
     * initialize the goals that the agents has
     */
    private void initGoals(){
        this.goals = new ArrayList<Relation>();
    }

    /**
     * initialize the associations between properties
     */
    private void initAssociations(){
        this.associations = new HashMap<String, String>();
        this.associations.put("location", "contains");
    }

    /**
     * initialize the set of known locations
     */
    private void initKnownLocations(){
        this.knownLocations = new HashMap<String, double[]>();
        this.knownLocations.put("breakroom", breakroomLoc);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////   Registry and Component communication    /////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * get a reference to the TTS component
     * @return tts component reference
     */
    protected Object getRefTTS(){
        try {
            return componentRefs.get(Class.forName(tts));
        }
        catch(Exception e) {
            System.out.println(dido + "Could not get reference to " + tts);
            return null;
        }
    }

    /**
     * get a reference to the Motion component
     * @return motion component reference
     */
    protected Object getRefMotion(){
        try {
            return componentRefs.get(Class.forName(motion));
        }
        catch(Exception e) {
            System.out.println(dido + "Could not get reference to " + motion);
            return null;
        }
    }

    /**
     * get a reference to the Pioneer component
     * @return pioneer component reference
     */
    protected Object getRefPioneer(){
        try {
            return componentRefs.get(Class.forName(pioneer));
        }
        catch(Exception e) {
            System.out.println(dido + "Could not get reference to " + pioneer);
            return null;
        }
    }

    /**
     * connect to all components in componentRefs
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
            System.out.println(dido + "Attempting to connect to " + component);
            res = getClient(component);
            if (res == null){
                try {
                    Thread.sleep(2000);
                    System.out.println(dido + "sleeping on connection attempt ...");
                } catch(Exception e) {
                    System.out.println(dido + "ERROR: Can't sleep!");
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Connection established with " + getRefID(res));
        return res;
    }

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
        System.out.println(dido + "connected to " + user);
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
                    System.out.println(dido +"connected to requested component "+joiningComponent);
                    componentRefs.put(c, ref);
                    break;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean localrequestShutdown(Object credentials) { return false; }

    @Override
    protected boolean localServicesReady() {
        Boolean haveAllReferences = true;
        for(Class c : componentRefs.keySet())
            if(componentRefs.get(c)==null){
                haveAllReferences = false;
            }
        return haveAllReferences && initialized;
    }

    @Override
    protected void componentDownReact(String serverkey, String[][] constraints) {
        for (Class comp : componentRefs.keySet()) {
            if (getTypeFromID(serverkey).equalsIgnoreCase(getTypeFromID(getRefID(componentRefs.get(comp))))){
                System.out.println(dido + "lost connection to " + comp);
                ref = null;
            }
        }
    }

    @Override
    protected boolean clientDownReact(String user) { return false; }

    @Override
    public String additionalUsageInfo() { return ""; }

    @Override
    protected void localshutdown() {}

    @Override
    public void updateFromLog(String s) {}

    @Override
    public boolean parseadditionalargs(String[] args) {
        return false;
    }
}
