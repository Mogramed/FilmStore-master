package Services;

import Entities.Film;
import Manager.CSVManager;
import Manager.FilmManager;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class FilmDisplay {
    protected JFrame frame;
    private FilmManager filmManager;
    protected JPanel filmPanel;

    public FilmDisplay(FilmManager filmManager) {
        this.filmManager = filmManager;
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Film Display");
        filmPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ImageIcon refreshIcon = new ImageIcon(new ImageIcon("./FilmStore-master/src/util/refresh.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        JButton refreshButton = new JButton(refreshIcon);

        JButton newFilmButton = new JButton("Add New Film");
        newFilmButton.addActionListener(e -> filmManager.createAndAddFilm());
        topPanel.add(newFilmButton);

        refreshButton.setPreferredSize(new Dimension(30, 30));
        refreshButton.addActionListener(e -> refreshFilmDisplay());
        topPanel.add(refreshButton);


        frame.add(topPanel, BorderLayout.NORTH);
        filmPanel.setLayout(new WrapLayout(FlowLayout.LEFT));

        List<Film> films = filmManager.getFilms();
        films.forEach(film -> filmPanel.add(createFilmCard(film)));

        JScrollPane scrollPane = new JScrollPane(filmPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);

        refreshFilmDisplay();
    }




    JPanel createFilmCard(Film film) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.PAGE_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(255, 255, 255));
        card.setMaximumSize(new Dimension(200, 600));

        JLabel imageLabel = createImageLabel(film.getImageURL());
        card.add(imageLabel);

        addToCard(card, new JLabel("Title: " + film.getTitle()), true);
        addToCard(card, new JLabel("Year: " + film.getProductionYear()), false);
        addToCard(card, new JLabel("Director: " + String.join(", ", film.getDirector())), false);
        addToCard(card, new JLabel("Price: $" + film.getPrice()), true);

        // Add comments
        if (film.getCommentsForFilm(film.getCode()) != null) {
            film.getCommentsForFilm(film.getCode()).stream().limit(3).forEach(comment -> {
                // Assurez-vous que le commentaire et la note sont séparés correctement
                String[] parts = comment.getText().split(",", 2); // Split en deux parties, au cas où le commentaire contient des virgules
                String commentText = parts[0];
                String rating = parts.length > 1 ? parts[1] : "0"; // Prend la deuxième partie pour la note, ou "0" si non disponible
                String commentDisplay = renderStars(rating) + " " + commentText; // Affiche les étoiles suivies du texte
                addToCard(card, new JLabel(commentDisplay), false);
            });
        }


        JButton addCommentButton = new JButton("Add Comment");
        addCommentButton.addActionListener(e -> addComment(film));
        JButton detailsButton = new JButton("Details");
        JButton addToCartButton = new JButton("Add to Cart");
        card.add(createButtonPanel(detailsButton, addCommentButton, addToCartButton));

        detailsButton.addActionListener(e -> showFilmDetails(film));
        addToCartButton.addActionListener(e -> addToCart(film));

        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        return card;
    }

    private String renderStars(String ratingStr) {
        int rating;
        try {
            rating = Integer.parseInt(ratingStr); // Tente de convertir la note en entier
        } catch (NumberFormatException e) {
            System.err.println("Error parsing rating: " + ratingStr);
            rating = 0; // Utilisez 0 comme valeur par défaut si la note n'est pas un entier valide
        }

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }



    private void addComment(Film film) {
        // Demander le texte du commentaire
        String commentText = JOptionPane.showInputDialog(frame, "Enter your comment:");
        if (commentText != null && !commentText.isEmpty()) {
            // Demander la note associée au commentaire
            String rating = askForRating();

            // Obtenir le nom d'utilisateur de l'utilisateur actuel (exemple de récupération à adapter selon votre application)
            String username = getCurrentUserName();  // Cette méthode devrait retourner le nom de l'utilisateur actuel

            // Ajouter le commentaire avec le nom de l'utilisateur
            boolean success = CSVManager.addCommentToFilm(film.getCode(), commentText, rating, username);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Comment added successfully!");
                refreshFilmDisplay();  // Rafraîchit l'affichage pour montrer le nouveau commentaire
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to add comment.");
            }
        }
    }

    private String getCurrentUserName() {
        // Cette méthode doit être implémentée pour récupérer le nom d'utilisateur actuel
        // Retourne un exemple par défaut, à remplacer par votre logique de gestion d'utilisateur
        return "UserName";
    }


    private String askForRating() {
        Object[] options = {"1", "2", "3", "4", "5"};
        int selectedIndex = -1;

        while (selectedIndex == -1) {  // Continuer à demander jusqu'à ce qu'une option soit sélectionnée
            selectedIndex = JOptionPane.showOptionDialog(null, "How would you rate this film?",
                    "Rating", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[4]);

            // Si l'utilisateur ferme la boîte de dialogue ou appuie sur Cancel, selectedIndex sera -1
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(null, "Please select a rating to continue.",
                        "Rating Required", JOptionPane.WARNING_MESSAGE);
            }
        }

        return String.valueOf(selectedIndex + 1);  // Retourne la note sélectionnée (1-5)
    }


    private JLabel createImageLabel(String imageUrl) {
        JLabel imageLabel = new JLabel();
        try {
            URL url = new URL(imageUrl);
            ImageIcon imageIcon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(150, 225, Image.SCALE_SMOOTH));
            imageLabel.setIcon(imageIcon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        } catch (Exception e) {
            imageLabel.setText("Image not available");
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
        }
        return imageLabel;
    }

    private void addToCard(JPanel card, JComponent component, boolean isBold) {
        if (isBold) {
            Font boldFont = component.getFont().deriveFont(Font.BOLD);
            component.setFont(boldFont);
        }
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(component);
    }

    private JPanel createButtonPanel(JButton... buttons) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        for (JButton button : buttons) {
            button.setFocusPainted(false);
            buttonPanel.add(button);
        }
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return buttonPanel;
    }


    private void showFilmDetails(Film film) {
        JDialog detailsDialog = new JDialog(frame, "Film Details", true);
        detailsDialog.setLayout(new BorderLayout());

        JLabel imageLabel = createImageLabel(film.getImageURL());
        detailsDialog.add(imageLabel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        detailsPanel.add(new JLabel("Title: " + film.getTitle()));
        detailsPanel.add(new JLabel("Year: " + film.getProductionYear()));
        detailsPanel.add(new JLabel("Director: " + String.join(", ", film.getDirector())));
        detailsPanel.add(new JLabel("Actors: " + String.join(", ", film.getMainactors())));
        detailsPanel.add(new JLabel("Country: " + film.getCountry()));
        detailsPanel.add(new JLabel("Price: " + film.getPrice() + " euros "));
        detailsPanel.add(new JLabel("Description: " + film.getDescription()));
        detailsPanel.add(new JLabel("Commentaires: \n" + film.getCommentsForFilm(film.getCode())));

        JScrollPane scrollPane = new JScrollPane(detailsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailsDialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> detailsDialog.dispose());
        detailsDialog.add(closeButton, BorderLayout.SOUTH);

        detailsDialog.pack();
        detailsDialog.setLocationRelativeTo(frame);
        detailsDialog.setVisible(true);
    }



    public void refreshFilmDisplay() {
        filmPanel.removeAll();

        List<Film> updatedFilms = filmManager.getAllFilms();
        for (Film film : updatedFilms) {
            filmPanel.add(createFilmCard(film));
        }
        filmPanel.revalidate();
        filmPanel.repaint();
    }

    private void addToCart(Film film) {
        // Implémenter ici
        JOptionPane.showMessageDialog(frame, film.getTitle() + " added to cart!");
    }






}
