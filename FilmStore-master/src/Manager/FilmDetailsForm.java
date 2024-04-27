package Manager;

import Entities.Comment;
import Entities.Film;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;


public class FilmDetailsForm {
    private Film film;
    private JTextField titleField = new JTextField(20);
    private JTextField yearField = new JTextField(4);
    private JTextField directorsField = new JTextField(20);
    private JTextField actorsField = new JTextField(20);
    private JTextField countryField = new JTextField(15);
    private JTextField priceField = new JTextField(5);
    private JTextField durationField = new JTextField(5);
    private JTextArea descriptionArea = new JTextArea(5, 20);
    private JTextField themeField = new JTextField(20);
    private JTextField producersField = new JTextField(20);
    private JTextField imageURLField = new JTextField(20);
    private JPanel commentsPanel;
    private JButton saveButton;
    private JButton deleteButton;
    private JButton newFilmButton;

    public FilmDetailsForm(Film film) {
        this.film = (film != null) ? film : Film.createEmptyFilm();
        initializeCommentsPanel();
        populateFields(this.film);
        setupButtons();
    }

    private void initializeCommentsPanel() {
        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
    }

    private void populateFields(Film film) {
        if (film != null) {
            titleField.setText(film.getTitle());
            themeField.setText(String.join(", ", film.getTheme()));
            directorsField.setText(String.join(", ", film.getDirector()));
            producersField.setText(String.join(", ", film.getProducers()));
            actorsField.setText(String.join(", ", film.getMainactors()));
            countryField.setText(film.getCountry());
            yearField.setText(film.getProductionYear());
            durationField.setText(film.getDurationMinutes());
            priceField.setText(film.getPrice());
            descriptionArea.setText(film.getDescription());
            imageURLField.setText(film.getImageURL());
            // Update comments display
            updateCommentsDisplay(film.getComments());
        } else {
            titleField.setText("");
            themeField.setText("");
            directorsField.setText("");
            producersField.setText("");
            actorsField.setText("");
            countryField.setText("");
            durationField.setText("");
            priceField.setText("");
            descriptionArea.setText("");
            imageURLField.setText("");
            yearField.setText("");
            commentsPanel.removeAll();
        }
    }

    private void updateCommentsDisplay(List<Comment> comments) {
        commentsPanel.removeAll();
        if (comments != null) {
            for (Comment comment : comments) {
                String displayText = renderStars(comment.getRating()) + " " + comment.getText();
                JLabel commentLabel = new JLabel(displayText);
                commentsPanel.add(commentLabel);
            }
        }
        commentsPanel.revalidate();
        commentsPanel.repaint();
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


    private void setupButtons() {
        saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveChanges());

        deleteButton = new JButton("Delete Film");
        deleteButton.addActionListener(e -> deleteFilm());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        commentsPanel.add(buttonPanel);
    }

    private void saveChanges() {
        try {
            Film updatedFilm = updateFilmFromForm();
            boolean success = CSVManager.updateFilmInCSV(updatedFilm);
            if (success) {
                JOptionPane.showMessageDialog(null, "Film updated successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to update film.");
            }
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, "Error parsing film details.");
        }
    }

    private void deleteFilm() {
        int response = JOptionPane.showConfirmDialog(null, "Confirm delete for film: " + film.getTitle() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            boolean success = CSVManager.deleteFilmFromCSV(film.getCode());
            if (success) {
                JOptionPane.showMessageDialog(null, "Film deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to delete film.");
            }
        }
    }



    public JPanel getFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 4, 4, 4);

        formPanel.add(new JLabel("Title:"), gbc);
        formPanel.add(titleField, gbc);
        formPanel.add(new JLabel("Theme(s):"), gbc);
        formPanel.add(themeField, gbc);
        formPanel.add(new JLabel("Year:"), gbc);
        formPanel.add(yearField, gbc);
        formPanel.add(new JLabel("Director(s):"), gbc);
        formPanel.add(directorsField, gbc);
        formPanel.add(new JLabel("Actor(s):"), gbc);
        formPanel.add(actorsField, gbc);
        formPanel.add(new JLabel("Country:"), gbc);
        formPanel.add(countryField, gbc);
        formPanel.add(new JLabel("Duration (minutes):"), gbc);
        formPanel.add(durationField, gbc);
        formPanel.add(new JLabel("Producer(s):"), gbc);
        formPanel.add(producersField, gbc);
        formPanel.add(new JLabel("Price:"), gbc);
        formPanel.add(priceField, gbc);
        formPanel.add(new JLabel("Description:"), gbc);
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        formPanel.add(new JLabel("Comments:"), gbc);
        formPanel.add(new JScrollPane(commentsPanel), gbc);

        return formPanel;
    }

    public Film updateFilmFromForm() throws ParseException {
        String title = titleField.getText();
        List<String> theme = Arrays.asList(themeField.getText().split("\\s*,\\s*"));
        List<String> directors = Arrays.asList(directorsField.getText().split("\\s*,\\s*"));
        List<String> producers = Arrays.asList(producersField.getText().split("\\s*,\\s*"));
        List<String> actors = Arrays.asList(actorsField.getText().split("\\s*,\\s*"));
        String country = countryField.getText();
        String price = priceField.getText();
        String description = descriptionArea.getText();
        String imageURL = imageURLField.getText();
        String productionYear = yearField.getText();
        String duration = durationField.getText();

        Vector<Comment> comments = extractCommentsFromUI();

        return new Film(film.getCode(), title, theme, description, directors, producers, actors, productionYear, duration, country, price, imageURL, comments);
    }

    private Vector<Comment> extractCommentsFromUI() {
        Vector<Comment> comments = new Vector<>();
        Component[] commentComponents = commentsPanel.getComponents();
        for (Component comp : commentComponents) {
            if (comp instanceof JPanel) {
                Component[] componentsInPanel = ((JPanel) comp).getComponents();
                // Assurer que les composants sont JLabel avant de les caster
                if (componentsInPanel.length >= 3 &&
                        componentsInPanel[0] instanceof JLabel &&
                        componentsInPanel[2] instanceof JLabel &&
                        componentsInPanel[1] instanceof JLabel) {

                    JLabel textLabel = (JLabel) componentsInPanel[0]; // JLabel du texte
                    JLabel nameLabel = (JLabel) componentsInPanel[2]; // JLabel du nom de l'utilisateur
                    JLabel starLabel = (JLabel) componentsInPanel[1]; // JLabel des étoiles

                    String text = textLabel.getText();
                    String userName = nameLabel.getText();
                    int rating = parseRatingFromStars(starLabel.getText()); // Méthode pour extraire le rating à partir du texte des étoiles

                    comments.add(new Comment(text, Integer.toString(rating), film.getCode(), userName));
                }
            }
        }
        return comments;
    }



    private int parseRatingFromStars(String text) {
        int rating = 0;
        for (char c : text.toCharArray()) {
            if (c == '★') {
                rating++;
            }
        }
        return rating;
    }




}
