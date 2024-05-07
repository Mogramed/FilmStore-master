package Entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Comment {
    private String filmcode;
    private String text;
    private String rating ;
    private String usercode;


    public Comment(String text, String rating, String filmcode, String usercode) {
        this.text = text;
        this.rating = rating;
        this.filmcode = filmcode;
        this.usercode = usercode;
    }

    public String getFilmcode() {
        return filmcode;
    }

    public String getUsercode() {
        return usercode;
    }

    public String getFilmCode() {
        return filmcode;
    }

    public String getText() {
        return text;
    }

    public String getRating() {
        return rating;
    }


    public static String getUserNameFromUserId(String userId) {
        // Tentez d'abord de trouver l'utilisateur dans la base des utilisateurs normaux
        String userName = findUserNameInFile(userId, "./FilmStore-master/src/CSVBase/users.csv");
        if (!"Unknown User".equals(userName)) {
            return userName;
        }

        // Si non trouvé, tentez de le trouver dans la base des administrateurs
        return findUserNameInFile(userId, "./FilmStore-master/src/CSVBase/admins.csv");

    }

    private static String findUserNameInFile(String userId, String filePath) {
        File userFile = new File(filePath);
        try (Scanner scanner = new Scanner(userFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] userDetails = line.split(";");
                if (userDetails[0].trim().equals(userId.trim())) {
                    return userDetails[1] + " " + userDetails[2]; // firstname and lastname
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + userFile.getAbsolutePath());
        }
        return "Unknown User";
    }
}
