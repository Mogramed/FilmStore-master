package Entities;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class User {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String address;
    private String passphrase;
    private int phonenumber;
    private List<Film> historiqueAchats;
    private List<String> comments;
    private boolean isAdmin;


    public User(String id, String firstname, String lastname, String email, String password, String address, String passphrase, int phonenumber, boolean isAdmin) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.address = address;
        this.passphrase = passphrase;
        this.phonenumber = phonenumber;
        this.comments = new ArrayList<>();
        this.historiqueAchats = new ArrayList<>();
        this.isAdmin = false;

    }

    public int getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(int phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAdress(String adress) {
        this.address = adress;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public List<Film> getHistoriqueAchats() {return historiqueAchats;}

    public void setHistoriqueAchats(List<Film> historiqueAchats) {this.historiqueAchats = historiqueAchats;}

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }



}
