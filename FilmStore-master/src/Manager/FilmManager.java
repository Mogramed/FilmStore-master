package Manager;
import Entities.*;

import java.io.*;
import java.util.*;


public class FilmManager {
    private List<Film> films;
    private Map<String, List<Comment>> filmComments;
    private static final String FILM_CSV_FILE_PATH = "./FilmStore-master/src/CSVBase/films.csv";

    public FilmManager() {

        filmComments = new HashMap<>();
        this.films = new ArrayList<>();
    }


    public void deleteFilm(String code) {
        List<String> remainingLines = new ArrayList<>();
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILM_CSV_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(code + ";")) {
                    remainingLines.add(line);
                } else {
                    found = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        if (found) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(FILM_CSV_FILE_PATH))) {
                for (String line : remainingLines) {
                    writer.println(line);
                }
            } catch (IOException e) {
                System.err.println("Error writing file: " + e.getMessage());
            }
            System.out.println("Film deleted successfully.");
        } else {
            System.out.println("Film not found.");
        }
    }
    public List<Film> getAllFilms() {
        return new ArrayList<>(films);
    }




    public List<Film> getFilms() {
        return films;
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

    // Méthode pour obtenir le nom d'utilisateur à partir du code utilisateur
    public String getUserNameFromUserCode(String userCode) {
        File userFile = new File("./FilmStore-master/src/CSVBase/users.csv");
        try (Scanner scanner = new Scanner(userFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] userDetails = line.split(";");  // Assurez-vous que le délimiteur est correct
                if (userDetails[0].trim().equals(userCode.trim())) {
                    return userDetails[1];  // Supposons que le nom est dans la deuxième colonne
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + userFile.getAbsolutePath());
        }
        return "Unknown User";  // Nom par défaut si le code utilisateur n'est pas trouvé
    }



    public void displayFilmComments() {
        List<Film> films = loadFilms();  // Assurez-vous que cette méthode charge aussi les commentaires

        for (Film film : films) {
            System.out.println("Film: " + film.getTitle());
            List<Comment> comments = film.getCommentsForFilm(film.getCode());

            for (Comment comment : comments) {
                System.out.println("   Comment: " + comment.getText() + " - " + renderStars(comment.getRating()));
            }

            System.out.println();
        }
    }

    private String renderStars(String rating) {
        int intRating;
        try {
            intRating = Integer.parseInt(rating);  // Convertit la chaîne en entier
        } catch (NumberFormatException e) {
            return "Invalid rating";  // Gère les cas où la chaîne ne peut pas être convertie en entier
        }

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < intRating; i++) {
            stars.append("★");
        }
        for (int i = intRating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }


}