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

    public void sperren() {
        this.state = State.BOOKED;
    }

    public void aktualisiereKapazitaet(int neueKapazitaet) {
        this.capacity = neueKapazitaet;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    
}