package de.campusflow.fachlogik;

import java.util.List;
import java.util.Optional;

import de.campusflow.model.Room;

public interface RoomRepository {

    public void save(Room room);
    public Optional<Room> findById(Long roomId);
    public List<Room> findAll(String roomFilter);
    public void delete(Long roomId);
}
