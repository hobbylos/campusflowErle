package de.campusflow.darstellung.zugangskontrolle;

import de.campusflow.fachlogik.zugangskontrolle.AuthService;
import org.springframework.web.bind.annotation.*;

/** EC-14 "Login mit Uni-Account". */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginAntwortDto login(@RequestBody LoginAnfrageDto anfrage) {
        AuthService.LoginErgebnis ergebnis = authService.login(anfrage.uniKennung(), anfrage.credential());
        return new LoginAntwortDto(ergebnis.token(), ergebnis.gueltigBis(), NutzerDto.von(ergebnis.nutzer()));
    }
}
