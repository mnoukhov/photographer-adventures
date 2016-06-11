package com.google.sample.cloudvision;

/**
 * Created by Jeric Pauig on 6/11/2016.
 */
public class Object {
    public enum State {CORRECT, SKIPPED, NOT_TESTED};

    private String name;
    private State state;
    private int attempts;

    public Object(String name, State state, int attempts){
        this.name = name;
        this.state = state;
        this.attempts = attempts;
        //TODO: validate attempts if state is correct or skipped or whatever
    }
}
