package de.campusflow.modulZugangskontrolle;

import java.util.Set;

public class Role {
    private String name;
    private Set<Action> berechtigungen;

    public Role(String name, Set<Action> berechtigungen){
        this.name = name;
        this.berechtigungen = berechtigungen;
    }

    public String getName(){
        return name;
    }

    public Set<Action> getBerechtigungen(){
        return berechtigungen;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setBerechtigungen(Set<Action> berechtigungen){
        this.berechtigungen = berechtigungen;
    }
}
