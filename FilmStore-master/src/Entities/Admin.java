package Entities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Admin {
    private String adminid;
    private String firstname;
    private String lastname;
    private String email ;
    private String password;
    private String adminToken;
    private boolean isAdmin;

    public Admin(String adminid, String firstname, String lastname, String email, String password, String adminToken, boolean isAdmin) {
        this.adminid= adminid;
        this.firstname = firstname;
        this.lastname = lastname ;
        this.email = email;
        this.password = password;
        this.adminToken = adminToken;
        this.isAdmin = true;
    }

    public String getAdminid() {
        return adminid;
    }

    public void setAdminid(String adminid) {
        this.adminid = adminid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdminToken() {
        return adminToken;
    }

    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }


    private static List<Admin> admins = new ArrayList<>(); // Liste pour stocker les administrateurs


    // Getter pour la liste des administrateurs
    public static List<Admin> getAdmins() {
        return admins;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public static void setAdmins(List<Admin> admins) {
        Admin.admins = admins;
    }


}
