package Entities;

import Manager.CSVManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Film {
    private String code;
    private String title;
    private List<String> theme;
    private String description;
    private List<String> director;
    private List<String> producers;
    private List<String> mainactors;
    private String productionYear;
    private String durationMinutes;
    private String country;
    private String price ;
    private String imageURL;
    private List<Comment> comments;


    public Film(String code, String title, List<String> theme, String description, List<String> director, List<String> producers, List<String> mainactors, String productionYear, String durationMinutes, String country, String price, String imageURL, List<Comment> comments) {
        this.code = code;
        this.title = title;
        this.theme = theme;
        this.description = description;
        this.director = director;
        this.producers = producers;
        this.mainactors = mainactors;
        this.productionYear = productionYear;
        this.durationMinutes = durationMinutes;
        this.country = country;
        this.price = price;
        this.imageURL = imageURL;
        this.comments = new ArrayList<>();
    }

    // Dans votre classe Film
    public static Film createEmptyFilm() {
        String newId = CSVManager.generateNextFilmId();
        return new Film(newId, "", Arrays.asList(""), "", Arrays.asList(""), Arrays.asList(""), Arrays.asList(""), "", "", "", "", "", new ArrayList<>());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTheme() {
        return theme;
    }

    public void setTheme(List<String> theme) {
        this.theme = theme;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getDirector() {
        return director;
    }

    public void setDirector(List<String> director) {
        this.director = director;
    }

    public List<String> getProducers() {
        return producers;
    }

    public void setProducers(List<String> producers) {
        this.producers = producers;
    }

    public List<String> getMainactors() {
        return mainactors;
    }

    public void setMainactors(List<String> mainactors) {
        this.mainactors = mainactors;
    }

    public String getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(String productionYear) {
        this.productionYear = productionYear;
    }

    public String getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(String durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageURL() {return imageURL;}


    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    // Lors de l'extraction des commentaires à partir de la ligne CSV
    public List<Comment> getCommentsForFilm(String filmCode) {
        File file = new File("./FilmStore-master/src/CSVBase/films.csv");
        List<Comment> comments = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(filmCode + ";")) {
                    String[] parts = line.split(";");
                    if (parts.length > 12 && !parts[12].isEmpty()) {
                        String[] allComments = parts[12].split("\\|");
                        for (String singleComment : allComments) {
                            String[] commentDetails = singleComment.split(",", 3);
                            if (commentDetails.length == 3) {
                                comments.add(new Comment(commentDetails[0].trim(), commentDetails[1].trim(),filmCode, commentDetails[2].trim()));
                            }
                        }
                    }
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + file.getAbsolutePath());
        }
        return comments;
    }



}
