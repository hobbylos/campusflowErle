package de.campusflow.modulZugangskontrolle;

import java.util.LinkedList;
import java.util.List;

public class User {
    
    private int id;
    private String name;
    private List<Role> roles;

    public User(int id, String name, Role[] roles){
        this.id = id;
        this.name = name;
        this.roles = new LinkedList<>();
        for(int i = 0; i < roles.length; i++){
            this.roles.add(roles[i]);
        }
    }
    
    public int getId(){
        return this.id;
    }
    
    public void setRoles(Role role){
        this.roles.add(role);
    }

    public List<Role> getRoles(){
        return this.roles;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
