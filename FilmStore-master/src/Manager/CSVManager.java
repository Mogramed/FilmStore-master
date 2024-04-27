package Manager;


import Entities.*;

import java.io.*;
import java.security.cert.Extension;
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
        return "USR" + (lineCount + 1);  // Prefix 'USR' with the next line number
    }


    public static String getUserNameFromUserId(String userId) {
        File userFile = new File("./FilmStore-master/src/CSVBase/users.csv");
        try (Scanner scanner = new Scanner(userFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] userDetails = line.split(";");  // Assurez-vous que le séparateur est correct
                if (userDetails.length > 1 && userDetails[0].trim().equals(userId.trim())) {
                    return userDetails[1];  // Supposons que le nom de l'utilisateur est à l'index 1
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + userFile.getAbsolutePath());
        }
        return "Unknown User";  // Retourne un nom par défaut si l'utilisateur n'est pas trouvé
    }

    public static boolean addCommentToFilm(String filmCode, String comment, String rating, String userid) {
        File file = new File(FILM_CSV_FILE_PATH);
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        String userName = getUserNameFromUserId(userid);// Récupérer le nom de l'utilisateur
        System.out.println(userName);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(filmCode + ";")) {
                    String[] parts = line.split(";", -1);

                    // Construire le commentaire avec le nom d'utilisateur
                    String newComment = comment + "," + rating + "," + userName;

                    // Gérer l'ajout ou la mise à jour des commentaires
                    if (parts.length <= 12 || parts[12].isEmpty()) {
                        line += ";" + newComment;
                    } else {
                        String existingComments = parts[12];
                        parts[12] = existingComments.isEmpty() ? newComment : existingComments + "|" + newComment;
                        line = String.join(";", parts);
                    }
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




}
