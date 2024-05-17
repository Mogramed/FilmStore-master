package Manager;


import Entities.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CSVManager {
    private static final String USER_CSV_FILE_PATH = "./FilmStore-master/src/CSVBase/users.csv";
    private static final String ADMIN_CSV_FILE_PATH = "./FilmStore-master/src/CSVBase/admins.csv";
    private static final String FILM_CSV_FILE_PATH = "./FilmStore-master/src/CSVBase/films.csv";



    public static String generateNextId(String filePath) {
        File file = new File(filePath);
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.readLine() != null) {
                lineCount++;
            }
        } catch (IOException e) {
            System.err.println("Error reading the file to generate ID: " + filePath);
            e.printStackTrace();
        }

        // Déterminer le préfixe en fonction du fichier
        String prefix = filePath.contains("admins") ? "ADM" : "USR";
        return prefix + (lineCount + 1);  // Générer l'ID avec le préfixe approprié
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



    // Ajoute un commentaire à la fois dans le fichier des films et des utilisateurs
    public static boolean addCommentToFilmAndUser(String filmCode, String comment, String rating, String userId) {
        if (addCommentToFilm(filmCode, comment, rating, userId)) {
            return addCommentToUser(userId, comment, rating, filmCode);
        }
        return false;
    }

    // Ajoute un commentaire dans la base de données des utilisateurs
    public static boolean addCommentToUser(String userId, String comment, String rating, String filmCode) {
        File file = new File(USER_CSV_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        String newCommentEntry = comment + "," + rating + "," + filmCode;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(userId + ";")) {
                    String[] parts = line.split(";", -1); // Inclut les colonnes vides à la fin
                    if (parts.length > 8) { // Assuming comments are at index 8
                        parts[8] = parts[8].isEmpty() ? newCommentEntry : parts[8] + "|" + newCommentEntry;
                    } else {
                        // Extend the array to include the comments column if it does not exist
                        String[] newParts = Arrays.copyOf(parts, 9);
                        newParts[8] = newCommentEntry;
                        parts = newParts;
                    }
                    line = String.join(";", parts);
                    updated = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Failed to read the file: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }

        if (updated) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (String modifiedLine : lines) {
                    writer.println(modifiedLine);
                }
            } catch (IOException e) {
                System.err.println("Failed to write to the file: " + file.getAbsolutePath());
                e.printStackTrace();
                return false;
            }
        }

        return updated;
    }


    // Supprime un commentaire de la base des films et des utilisateurs
    public static boolean removeCommentFromFilmAndUser(String filmCode, String userId, String rating, String commentText) {
        if (removeCommentFromFilm(filmCode, commentText, rating, userId)) {
            return removeCommentFromUser(userId, commentText, rating, filmCode);
        }
        return false;
    }

    public static boolean removeCommentFromUser(String userId, String comment, String rating, String filmCode) {
        File file = new File(USER_CSV_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean found = false;
        String targetCommentEntry = (comment.trim() + "," + rating.trim() + "," + filmCode.trim()).toLowerCase();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(userId + ";")) {  // Modification pour vérifier la présence de l'ID utilisateur dans la ligne
                    String[] parts = line.split(";", -1);
                    if (parts.length > 8 && !parts[8].isEmpty()) {
                        List<String> comments = Arrays.stream(parts[8].split("\\|"))
                                .map(String::trim) // Nettoie les espaces autour des commentaires
                                .collect(Collectors.toList());
                        boolean removed = comments.removeIf(c -> {
                            String cleanedComment = c.toLowerCase(); // Nettoyage et mise en minuscule pour comparaison
                            return cleanedComment.equals(targetCommentEntry);
                        });
                        if (removed) {
                            found = true;
                            parts[8] = String.join("|", comments); // Rejoindre les commentaires restants
                            line = String.join(";", parts);
                        }
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (found) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) { // Mode écrasement
                for (String modifiedLine : lines) {
                    writer.println(modifiedLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return found;
    }





    public static boolean addCommentToFilm(String filmCode, String comment, String rating, String userId) {
        File file = new File(FILM_CSV_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(filmCode + ";")) {
                    String[] parts = line.split(";", -1); // Utiliser -1 pour inclure les colonnes vides
                    String newComment = comment + "," + rating + "," + userId;

                    // Ajouter ou mettre à jour le commentaire existant
                    if (parts.length > 12) {
                        if (parts[12].isEmpty() || parts[12].equals("\"\"")) {
                            parts[12] = newComment;  // Directement ajouter le nouveau commentaire si vide ou juste des guillemets
                        } else {
                            // Supprimer les guillemets avant d'ajouter un nouveau commentaire si nécessaire
                            if (parts[12].startsWith("\"\"|")) {
                                parts[12] = parts[12].substring(3) + "|" + newComment;  // Supprime les guillemets vides et ajoute le nouveau commentaire
                            } else {
                                parts[12] += "|" + newComment;
                            }
                        }
                    } else {
                        // Assurer que le tableau a suffisamment de places pour inclure une colonne de commentaire
                        parts = Arrays.copyOf(parts, 13); // Étendre le tableau pour inclure la colonne de commentaire
                        parts[12] = newComment;
                    }
                    line = String.join(";", parts);
                    updated = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Failed to read the file: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }

        if (updated) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (String modifiedLine : lines) {
                    writer.println(modifiedLine);
                }
            } catch (IOException e) {
                System.err.println("Failed to write to the file: " + file.getAbsolutePath());
                e.printStackTrace();
                return false;
            }
        }

        return updated;
    }

    public static boolean removeCommentFromFilm(String filmCode, String comment, String rating, String userId) {
        File file = new File(FILM_CSV_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean found = false;
        String targetCommentEntry = comment + "," + rating + "," + userId; // Format du commentaire à supprimer.

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(filmCode + ";")) {
                    String[] parts = line.split(";", -1);
                    if (parts.length > 12 && !parts[12].isEmpty()) {
                        List<String> comments = new ArrayList<>(Arrays.asList(parts[12].split("\\|")));
                        if (comments.removeIf(c -> c.equals(targetCommentEntry))) { // Vérifie si le commentaire exact est trouvé et supprimé.
                            found = true;
                        }
                        parts[12] = String.join("|", comments); // Rejoindre les commentaires restants
                        line = String.join(";", parts);
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (found) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (String modifiedLine : lines) {
                    writer.println(modifiedLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return found;
    }



    public static String generateNextFilmId() {
        File file = new File(FILM_CSV_FILE_PATH);
        int maxId = 0;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");
                if (parts[0].startsWith("FLM")) {
                    int idNum = Integer.parseInt(parts[0].substring(3));  // Extrait le nombre après "FLM"
                    if (idNum > maxId) {
                        maxId = idNum;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + file.getAbsolutePath());
        }
        return "FLM" + String.format("%03d", maxId + 1);  // Format ID as FLM001, FLM002, etc.
    }


    public static boolean addFilmToCSV(Film newFilm) {
        boolean isAdded = false;
        // Construire la nouvelle ligne avec les données du nouveau film
        String newLine = newFilm.getCode() + ";" +
                newFilm.getTitle() + ";" +
                "\"" + String.join(",", newFilm.getTheme()) + "\"" + ";" +
                "\"" + newFilm.getDescription() + "\"" + ";" +
                "\"" + String.join(",", newFilm.getDirector()) + "\"" + ";" +
                "\"" + String.join(",", newFilm.getProducers()) + "\"" + ";" +
                "\"" + String.join(",", newFilm.getMainactors()) + "\"" + ";" +
                newFilm.getProductionYear() + ";" +
                newFilm.getDurationMinutes() + ";" +
                newFilm.getCountry() + ";" +
                newFilm.getPrice() + ";" +
                newFilm.getImageURL() + ";" +
                serializeComments(newFilm.getComments());

        try (FileWriter fw = new FileWriter(FILM_CSV_FILE_PATH, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(newLine);  // Écrit la nouvelle ligne à la fin du fichier
            isAdded = true;
        } catch (IOException e) {
            System.err.println("Failed to write to the file: " + FILM_CSV_FILE_PATH);
            e.printStackTrace();
            return false;
        }

        return isAdded;
    }



    public static boolean updateFilmInCSV(Film updatedFilm) {
        File file = new File(FILM_CSV_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean isUpdated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", -1);
                if (parts[0].equals(updatedFilm.getCode())) {
                    // Construire la nouvelle ligne avec les données mises à jour
                    String newLine = updatedFilm.getCode() + ";" +
                            updatedFilm.getTitle() + ";" +
                            "\"" + String.join(",", updatedFilm.getTheme()) + "\"" + ";" +
                            "\"" + updatedFilm.getDescription() + "\"" + ";" +
                            "\"" + String.join(",", updatedFilm.getDirector()) + "\"" + ";" +
                            "\"" + String.join(",", updatedFilm.getProducers()) + "\"" + ";" +
                            "\"" + String.join(",", updatedFilm.getMainactors()) + "\"" + ";" +
                            updatedFilm.getProductionYear() + ";" +
                            updatedFilm.getDurationMinutes() + ";" +
                            updatedFilm.getCountry() + ";" +
                            updatedFilm.getPrice() + ";" +
                            updatedFilm.getImageURL() + ";" +
                            // Gérer les commentaires si présents
                            serializeComments(updatedFilm.getCommentsForFilm(updatedFilm.getCode()));
                    line = newLine;
                    isUpdated = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Failed to read the file: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }

        if (isUpdated) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
                for (String modifiedLine : lines) {
                    writer.println(modifiedLine);
                }
            } catch (IOException e) {
                System.err.println("Failed to write to the file: " + file.getAbsolutePath());
                e.printStackTrace();
                return false;
            }
        }

        return isUpdated;
    }

    private static String serializeComments(List<Comment> comments) {
        return comments.stream()
                .map(c -> c.getText() + "," + c.getRating() + "," + c.getUsercode())
                .collect(Collectors.joining("|"));
    }


    public static List<Comment> getUserComments(String userId) {
        File file = new File(USER_CSV_FILE_PATH);
        List<Comment> comments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", -1);
                if (parts[0].equals(userId) && parts.length > 8 && !parts[8].isEmpty()) {
                    String[] commentEntries = parts[8].split("\\|");
                    for (String entry : commentEntries) {
                        String[] details = entry.split(",", 3);
                        if (details.length == 3) {
                            comments.add(new Comment(details[0].trim(), details[1].trim(), userId, details[2].trim()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return comments;
    }



    public static boolean deleteFilmFromCSV(String filmCode) {
        File file = new File(FILM_CSV_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean isDeleted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(filmCode + ";")) {
                    lines.add(line);
                } else {
                    isDeleted = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read the file: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }

        if (isDeleted) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
                for (String remainingLine : lines) {
                    writer.println(remainingLine);
                }
            } catch (IOException e) {
                System.err.println("Failed to write to the file: " + file.getAbsolutePath());
                e.printStackTrace();
                return false;
            }
        }

        return isDeleted;
    }


    public static boolean editCommentInFilmAndUser(String filmCode, String userId, String oldRating, String oldCommentText, String newRating, String newCommentText) {
        boolean filmEdited = editCommentInFilm(filmCode, userId, oldRating, oldCommentText, newRating, newCommentText);
        boolean userEdited = editCommentInUser(userId, filmCode, oldRating, oldCommentText, newRating, newCommentText);
        return filmEdited && userEdited;
    }


    public static boolean editCommentInFilm(String filmCode, String userId, String oldRating, String oldCommentText, String newRating, String newCommentText) {
        File file = new File(FILM_CSV_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean found = false;
        String targetCommentEntry = (oldCommentText.trim() + "," + oldRating.trim() + "," + userId.trim()).toLowerCase();
        String newCommentEntry = newCommentText.trim() + "," + newRating.trim() + "," + userId.trim();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().startsWith(filmCode.toLowerCase() + ";")) {
                    String[] parts = line.split(";", -1);
                    if (parts.length > 12 && !parts[12].isEmpty()) {
                        List<String> comments = Arrays.stream(parts[12].split("\\|"))
                                .map(String::trim)
                                .collect(Collectors.toList());
                        boolean edited = false;
                        for (int i = 0; i < comments.size(); i++) {
                            System.out.println("Checking comment: " + comments.get(i).toLowerCase());
                            if (comments.get(i).toLowerCase().equals(targetCommentEntry)) {
                                comments.set(i, newCommentEntry);
                                edited = true;
                                found = true;
                                break;
                            }
                        }
                        if (edited) {
                            parts[12] = String.join("|", comments);
                            line = String.join(";", parts);
                        }
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (found) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
                for (String modifiedLine : lines) {
                    writer.println(modifiedLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return found;
    }




    public static boolean editCommentInUser(String userId, String filmCode, String oldRating, String oldCommentText, String newRating, String newCommentText) {
        File file = new File(USER_CSV_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean found = false;
        String targetCommentEntry = (oldCommentText.trim() + "," + oldRating.trim() + "," + filmCode.trim()).toLowerCase();
        String newCommentEntry = newCommentText.trim() + "," + newRating.trim() + "," + filmCode.trim();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().startsWith(userId.toLowerCase() + ";")) {
                    String[] parts = line.split(";", -1);
                    if (parts.length > 8 && !parts[8].isEmpty()) {
                        List<String> comments = Arrays.stream(parts[8].split("\\|"))
                                .map(String::trim)
                                .collect(Collectors.toList());
                        boolean edited = false;
                        for (int i = 0; i < comments.size(); i++) {
                            System.out.println("Checking comment: " + comments.get(i).toLowerCase());
                            if (comments.get(i).toLowerCase().equals(targetCommentEntry)) {
                                comments.set(i, newCommentEntry);
                                edited = true;
                                found = true;
                                break;
                            }
                        }
                        if (edited) {
                            parts[8] = String.join("|", comments);
                            line = String.join(";", parts);
                        }
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (found) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
                for (String modifiedLine : lines) {
                    writer.println(modifiedLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return found;
    }

}
