package Manager;
import Entities.*;


import javax.swing.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;


public class FilmManager {
    private List<Film> films;
    private Map<String, List<Comment>> filmComments;
    private static final String FILM_CSV_FILE_PATH = "./FilmStore-master/src/CSVBase/films.csv";

    public FilmManager() {

        filmComments = new HashMap<>();
        this.films = new ArrayList<>();
    }


    public List<Film> getAllFilms() {
        return new ArrayList<>(films);
    }

    public List<Film> getFilms() {
        return films;
    }


    public void createAndAddFilm() {
        Film newFilm = Film.createEmptyFilm(); // Assurez-vous que cette méthode existe et crée un objet Film correctement initialisé
        FilmDetailsForm form = new FilmDetailsForm(newFilm);
        int result = JOptionPane.showConfirmDialog(null, form.getFormPanel(), "Add New Film", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Film finalFilm = form.updateFilmFromForm();
                if (addFilm(finalFilm)) {
                    JOptionPane.showMessageDialog(null, "New film added successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to add new film.");
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(null, "Error parsing new film details.");
            }
        }
    }

    public boolean addFilm(Film newFilm) {
        if (newFilm != null) {
            films.add(newFilm);
            return CSVManager.addFilmToCSV(newFilm);
        }
        return false;
    }


    // Method to get a film by its code
    public Film getFilmById(String filmCode) {
        for (Film film : films) {
            if (film.getCode().equals(filmCode)) {
                return film;
            }
        }
        return null; // Return null if no film matches the given ID
    }

    public List<Film> loadFilms() {
        List<Film> loadedFilms = new ArrayList<>();
        this.films = new ArrayList<>();
        File file = new File(FILM_CSV_FILE_PATH);
        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter(";|\n");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] details = line.split(";", -1);

                String code = details[0];
                String title = details[1];
                List<String> theme = Arrays.asList(details[2].replace("\"", "").split(","));
                String description = details[3].replace("\"", "");
                List<String> directors = Arrays.asList(details[4].replace("\"", "").split(","));
                List<String> producers = Arrays.asList(details[5].replace("\"", "").split(","));
                List<String> mainactors = Arrays.asList(details[6].replace("\"", "").split(","));
                String productionYear = details[7];
                String durationMinutes = details[8];
                String country = details[9];
                String price = details[10];
                String imageURL = details[11];
                // Handling comments
                List<Comment> comments = new ArrayList<>();
                if (details.length > 12 && !details[12].isEmpty()) {
                    String[] commentDetails = details[12].split("\\|");
                    for (String commentDetail : commentDetails) {
                        String[] commentParts = commentDetail.split(",", 3);
                        if (commentParts.length == 3) {
                            String commentText = commentParts[0].trim();
                            String rating = commentParts[1].trim();
                            String userId = commentParts[2].trim();
                            comments.add(new Comment(code, commentText, rating, userId));
                        }
                    }
                }

                Film film = new Film(code, title, theme, description, directors, producers, mainactors, productionYear, durationMinutes, country, price, imageURL, comments);
                loadedFilms.add(film);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find the file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        this.films.addAll(loadedFilms);
        return loadedFilms;
    }


    public boolean updateFilm(String code, Film updatedFilm) {
        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getCode().equals(code)) {
                films.set(i, updatedFilm);
                return CSVManager.updateFilmInCSV(updatedFilm);
            }
        }
        return false;
    }

    public boolean deleteFilm(String filmCode) {
        return CSVManager.deleteFilmFromAllBases(filmCode);
    }



}