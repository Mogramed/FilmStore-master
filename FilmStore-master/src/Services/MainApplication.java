package Services;

import Manager.*;
import javax.swing.*;


public class MainApplication {
    public static void main(String[] args) {

        FilmManager filmManager = new FilmManager();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginWindow(filmManager);
            }
        });
    }
}


