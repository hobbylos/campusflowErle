package de.campusflow.modulZugangskontrolle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Rolevalidator implements AuthorizationValidator {
    
    private Map<Role, Set<Action>> roleActions;

    public Rolevalidator(Role role, Set<Action> actions){
        this.roleActions = new HashMap<>();
        addRole(role, actions);        
    }

    public void addRole(Role role, Set<Action> actions){
        this.roleActions.put(role, actions);
    }

    private boolean validateAction(User user, Action action){
        List<Role> userRoles = user.getRoles();
        
        for(int i = 0; i < userRoles.size(); i++){
            if(roleActions.containsKey(userRoles.get(i)) && 
                roleActions.get(userRoles.get(i)).contains(action)){
                return true;
            }   
        }

        return false;
    }

    public boolean canManageRoom(User user, Action action){
        return validateAction(user, action);
    }
}
