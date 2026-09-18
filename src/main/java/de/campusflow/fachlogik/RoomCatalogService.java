package de.campusflow.fachlogik;

import java.util.List;

import de.campusflow.model.Room;
import de.campusflow.modulZugangskontrolle.Action;
import de.campusflow.modulZugangskontrolle.AuthorizationValidator;
import de.campusflow.modulZugangskontrolle.User;

public class RoomCatalogService implements RoomCatalogQuery{

    private RoomRepository roomRepository;
    private FacilityAdapter facilityAdapter;
    private AuthorizationValidator authorizationValidator;

    public RoomCatalogService(RoomRepository roomRepository, FacilityAdapter facilityAdapter, AuthorizationValidator authorizationValidator){
        this.roomRepository = roomRepository;
        this.facilityAdapter = facilityAdapter;
        this.authorizationValidator = authorizationValidator;
    }

    public void addRoom(Room room, User user) throws Exception{
        if(!authorizationValidator.canManageRoom(user, Action.CREATE)){
            throw new Exception("Keine Berechtigung");
        }
        roomRepository.save(room);
        facilityAdapter.raumErstellt(room);
    }

    public void updateRoom(Long roomId, Room room, User user) throws Exception{
        if(!authorizationValidator.canManageRoom(user, Action.UPDATE)){
            throw new Exception("Keine Berechtigung");
        }
        roomRepository.findById(roomId).orElseThrow(() -> new Exception("Raum nicht gefunden"));
        room.setId(roomId);
        roomRepository.save(room);
        facilityAdapter.raumAktualisiert(room);
    }

    public void deleteRoom(Long roomId, User user) throws Exception{
        if (!authorizationValidator.canManageRoom(user, Action.DELETE)){
            throw new Exception("Keine Berechtigung");
        }
        roomRepository.delete(roomId);
        facilityAdapter.raumGeloescht(roomId);
    }

    @Override
    public Room getRoom(Long roomId) throws Exception {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new Exception("Raum nicht gefunden"));
        return room;
    }

    @Override
    public List<Room> getRooms(String roomFilter) {
       List<Room> rooms = roomRepository.findAll(roomFilter);
       return rooms;
    }

}
