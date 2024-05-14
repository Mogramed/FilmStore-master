package Manager;

import Entities.*;

import java.io.*;
import java.util.Scanner;

import static Manager.CSVManager.generateNextId;

public class UserManager {
    private static final String USER_CSV_FILE_PATH = "./FilmStore-master/src/CSVBase/users.csv";
    private static final String ADMIN_CSV_FILE_PATH = "./FilmStore-master/src/CSVBase/admins.csv";



    public static User authenticate(String email, String password, String token, boolean isAdmin) {
        String filePath = isAdmin ? ADMIN_CSV_FILE_PATH : USER_CSV_FILE_PATH;
        File file = new File(filePath);
        System.out.println("Authenticating: " + (isAdmin ? "Admin" : "User"));

        // Le token admin est une constante, vous pourriez le définir quelque part dans vos configurations.
        final String ADMIN_TOKEN = "mabelle";

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] userDetails = line.split(";");
                System.out.println("Checking lines");
                if (userDetails[3].equals(email) && userDetails[4].equals(password)) {
                    System.out.println("Email and password match found");
                    if (!isAdmin || (isAdmin && token.equals(ADMIN_TOKEN))) {
                        System.out.println("Secret or token validation passed");
                        // Créer l'utilisateur ou l'administrateur sans numéro de téléphone ici
                        return new User(userDetails[0], userDetails[1], userDetails[2], userDetails[3], userDetails[4], "", "", 0, isAdmin);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find the file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return null; // Si l'authentification échoue
    }


    public static boolean addUser(User user) {
        String filePath = user.isAdmin() ? ADMIN_CSV_FILE_PATH : USER_CSV_FILE_PATH;
        File file = new File(filePath);

        // Générer l'ID suivant pour le nouvel utilisateur
        user.setId(generateNextId(filePath));

        // Vérifier si l'email est déjà utilisé
        if (emailExists(user.getEmail(), filePath)) {
            System.err.println("Account already exists.");
            return false;
        }

        // Écrire les données de l'utilisateur dans le fichier CSV
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            String userData = String.join(";", user.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getPassword(),
                    user.getAddress(), user.getPassphrase(), String.valueOf(user.getPhonenumber()));
            out.println(userData);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write to the file: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean emailExists(String email, String filePath) {
        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] userDetails = line.split(";");
                if (userDetails[3].equals(email)) {
                    return true; // Email déjà utilisé
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find the file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return false; // Aucun utilisateur avec cet email
    }

    public static boolean addAdmin(Admin admin) {
        String filePath = ADMIN_CSV_FILE_PATH;
        File file = new File(filePath);

        // Générer l'ID suivant pour le nouvel admin
        admin.setAdminid(generateNextId(filePath));

        // Vérifier si l'email est déjà utilisé
        if (emailExists(admin.getEmail(), filePath)) {
            System.err.println("Account already exists.");
            return false;
        }

        // Écrire les données de l'admin dans le fichier CSV
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            String userData = String.join(";", admin.getAdminid(), admin.getFirstname(), admin.getLastname(), admin.getEmail(), admin.getPassword());
            out.println(userData);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write to the file: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }
    }
}
