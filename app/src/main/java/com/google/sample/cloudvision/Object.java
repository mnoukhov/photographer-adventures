package com.google.sample.cloudvision;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class Object {
    public enum State {CORRECT, SKIPPED, NOT_TESTED};

    private String name;
    public State state;
    public int attempts;

    public Object(String name, State state, int attempts){
        this.name = name;
        this.state = state;
        this.attempts = attempts;
        //TODO: validate attempts if state is correct or skipped or whatever
    }

    public State getState(){
        return this.state;
    }

    public int getAttempts(){
        return this.attempts;
    }

    public String getName(){
        return this.name;
    }
}
