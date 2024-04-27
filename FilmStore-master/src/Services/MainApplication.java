package Services;
import Entities.*;
import Manager.*;
import javax.swing.*;
import java.util.List;

public class MainApplication {
    public static void main(String[] args) {

        FilmManager filmManager = new FilmManager();
        filmManager.displayFilmComments();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginWindow(filmManager);
            }
        });
    }
}


