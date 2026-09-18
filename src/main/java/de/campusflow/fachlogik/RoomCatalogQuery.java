package de.campusflow.fachlogik;

import java.util.List;

import de.campusflow.model.Room;

public interface RoomCatalogQuery {
    public Room getRoom(Long roomId) throws Exception;
    public List<Room> getRooms(String roomFilter) throws Exception;
}
