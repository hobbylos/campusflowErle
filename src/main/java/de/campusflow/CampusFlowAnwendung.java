package de.campusflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class CampusFlowAnwendung {

    public static void main(String[] args) {
        // Ermöglicht Desktop-Browser-Start auch in Umgebungen mit Headless-Voreinstellung
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(CampusFlowAnwendung.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowserOnStartup() {
        String url = "http://localhost:8080";
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Exception e) {
            System.out.println("[CampusFlow] Browser konnte nicht automatisch geöffnet werden: " + e.getMessage());
        }
    }
}
