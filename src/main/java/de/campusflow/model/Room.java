package de.campusflow.model;

import java.util.List;

public class Room {

    private Long id;
    private String name;
    private int capacity;
    private List<String> equipment;
    private State state;
    private String category;
    private String building;

    public Room(Long id, String name, int capacity, List<String> equipmentList, State state, String category, String building){
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.equipment = equipmentList;
        this.state = state;
        this.category = category;
        this.building = building;
    }

    public void sperren(){
        this.state = State.BOOKED;
    }

    public void setCapacity(int capacity){
        this.capacity = capacity;
    }
}