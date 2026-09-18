package de.campusflow.modulZugangskontrolle;

public interface AuthorizationValidator {
    public boolean canManageRoom(User user, Action action);
}
