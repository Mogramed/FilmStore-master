package Entities;

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


}
