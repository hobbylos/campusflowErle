package de.campusflow.fachlogik;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.campusflow.model.Room;
import de.campusflow.model.State;
import de.campusflow.modulZugangskontrolle.Action;
import de.campusflow.modulZugangskontrolle.AuthorizationValidator;
import de.campusflow.modulZugangskontrolle.Role;
import de.campusflow.modulZugangskontrolle.User;

@ExtendWith(MockitoExtension.class)
public class RoomCatalogServiceTest {

    @Mock private RoomRepository roomRepository;
    @Mock private FacilityAdapter facilityAdapter;
    @Mock private AuthorizationValidator authorizationValidator;


    private RoomCatalogService service;
    private User user;
    private Room room;

    @BeforeEach 
    void setUp(){
        service = new RoomCatalogService(roomRepository, facilityAdapter, authorizationValidator);
        user = new User(1, "Max", new Role[0]);
        room = new Room(1L, "Erle", 20, List.of("Beamer"), State.AVAILABLE, "Hörsaal", "Haus A");
    }

    @Test
    void addRoom_speichertUndBenachrichtigt_wennBerechtigt() throws Exception {
        when(authorizationValidator.canManageRoom(user, Action.CREATE)).thenReturn(true);

        service.addRoom(room, user);
        verify(roomRepository).save(room);
        verify(facilityAdapter).raumErstellt(room);
    }

    @Test 
    void addRoom_wirftException_wennNichtBerechtigt(){
        when(authorizationValidator.canManageRoom(user, Action.CREATE)).thenReturn(false);

        assertThrows(Exception.class, () -> service.addRoom(room, user));
        verifyNoInteractions(roomRepository, facilityAdapter);
    }

    @Test 
    void getRoom_wirftException_wennNichtGefunden(){
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.getRoom(99L));
    }
    
}
