package Manager;

import Entities.*;

import java.io.*;
import java.util.Scanner;

import static Manager.CSVManager.generateNextId;

public class UserManager {
    private static final String USER_CSV_FILE_PATH = "./FilmStore-master/src/CSVBase/users.csv";
    private static final String ADMIN_CSV_FILE_PATH = "./FilmStore-master/src/CSVBase/admins.csv";

    public static boolean authenticate(String email, String password, String secretPhrase, boolean isAdmin) {
        String filePath = isAdmin ? ADMIN_CSV_FILE_PATH : USER_CSV_FILE_PATH;
        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] userDetails = line.split(";");
                if (userDetails[3].equals(email) && userDetails[4].equals(password)) {
                    if (!isAdmin) {  // Utilisation de la phrase secrète uniquement pour les utilisateurs normaux
                        return userDetails[6].equals(secretPhrase);
                    }
                    return true;  // Les admins sont vérifiés uniquement par email et mot de passe
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find the file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean addUser(User user) {
        String filePath = user.isAdmin() ? ADMIN_CSV_FILE_PATH : USER_CSV_FILE_PATH;
        File file = new File(filePath);

        user.setId(generateNextId(filePath));

        if (authenticate(user.getEmail(), user.getPassword(), user.getPassphrase(), false)) {
            System.out.println("Account already exists.");
            return false;
        }

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

    public static boolean addAdmin(Admin admin) {
        String filePath = ADMIN_CSV_FILE_PATH;
        File file = new File(filePath);

        admin.setAdminid(generateNextId(filePath));

        if (authenticate(admin.getFirstname(), admin.getPassword(), null, true)) {
            System.out.println("Account already exists.");
            return false;
        }

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
