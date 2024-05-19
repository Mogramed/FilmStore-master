package Services;

import Entities.Comment;
import Entities.Film;
import Manager.CSVManager;
import Manager.CartManager;
import Manager.FilmManager;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FilmDisplay {
    protected JFrame frame;
    private FilmManager filmManager;
    protected JPanel filmPanel;
    protected Map<String, Boolean> commentsEnabledMap;

    private static final String COMMENTS_STATE_FILE_PATH = "./FilmStore-master/src/CSVBase/commentsEnabledState.csv";

    public FilmDisplay(FilmManager filmManager) {
        this.filmManager = filmManager;
        this.commentsEnabledMap = new HashMap<>();
        loadCommentsEnabledState(); // Charger l'état des commentaires activés/désactivés
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

        if (SessionContext.isUserAdmin()) {
            JButton newFilmButton = new JButton("Add New Film");
            newFilmButton.addActionListener(e -> filmManager.createAndAddFilm());
            topPanel.add(newFilmButton);
        }

        if (!SessionContext.isUserAdmin() && !SessionContext.isGuestUser()) {
            JButton viewCartButton = new JButton("View Cart");
            viewCartButton.addActionListener(e -> viewCart());  // Add an ActionListener to open the cart view
            topPanel.add(viewCartButton);

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton accountButton = new JButton("Account");
            accountButton.addActionListener(e -> openAccountPage());
            rightPanel.add(accountButton);
            topPanel.add(rightPanel, BorderLayout.EAST);
        }
        JButton loginButton = new JButton(SessionContext.isGuestUser() ? "Connect " : "Disconnect ");
        loginButton.addActionListener(e -> {
            if (SessionContext.isGuestUser()) {
                openLoginWindow();
            } else {
                logout();
            }
        });
        topPanel.add(loginButton);

        refreshButton.setPreferredSize(new Dimension(30, 30));
        refreshButton.addActionListener(e -> refreshFilmDisplay());
        topPanel.add(refreshButton);

        frame.add(topPanel, BorderLayout.NORTH);
        filmPanel.setLayout(new WrapLayout(FlowLayout.LEFT));

        List<Film> films = filmManager.getFilms();
        films.forEach(film -> {
            commentsEnabledMap.putIfAbsent(film.getCode(), true);  // Initialize commentsEnabled for each film
            filmPanel.add(createFilmCard(film));
        });

        JScrollPane scrollPane = new JScrollPane(filmPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);

        refreshFilmDisplay();
    }

    private void logout() {
        // Réinitialiser l'état de l'utilisateur
        SessionContext.setCurrentUser(null);
        SessionContext.setGuestUser(true);
        // Réinitialiser l'interface utilisateur
        //new LoginWindow(filmManager);
        frame.dispose();
    }

    protected void saveCommentsEnabledState() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COMMENTS_STATE_FILE_PATH))) {
            for (Map.Entry<String, Boolean> entry : commentsEnabledMap.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void loadCommentsEnabledState() {
        try (BufferedReader reader = new BufferedReader(new FileReader(COMMENTS_STATE_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    commentsEnabledMap.put(parts[0], Boolean.parseBoolean(parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (!SessionContext.isUserAdmin() && !SessionContext.isGuestUser()) {
            addToCard(card, new JLabel("Price: $" + getPriceWithDiscount(film.getPrice())), true);
        }else {
            addToCard(card, new JLabel("Price: $" + film.getPrice()), true);
        }
        // Calculate and display average rating
        double averageRating = calculateAverageRating(film.getCommentsForFilm(film.getCode()));
        JLabel ratingLabel = new JLabel(String.format("Average Rating: %.1f", averageRating));
        addToCard(card, ratingLabel, true);

        // Check if comments are enabled for this film
        Boolean commentsEnabled = commentsEnabledMap.get(film.getCode());
        if (commentsEnabled != null && commentsEnabled) {
            // Display up to three comments
            List<Comment> comments = film.getCommentsForFilm(film.getCode());
            if (comments != null && !comments.isEmpty()) {
                JPanel commentsPanel = new JPanel();
                commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
                // Limit to 3 comments view
                comments.stream().limit(3).forEach(comment -> {
                    JLabel commentLabel = new JLabel("<html><div style='width:300px;'><strong>" + comment.getUserNameFromUserId(comment.getUsercode()) + "</strong>: " + renderStars(comment.getRating()) +
                            "<br/>" + comment.getText() + "</div></html>");
                    commentsPanel.add(commentLabel);
                });
                JScrollPane scrollPane = new JScrollPane(commentsPanel);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setPreferredSize(new Dimension(180, 100)); // Adjust dimensions as needed
                card.add(scrollPane);

                if (!SessionContext.isGuestUser()) {
                    JButton commentsButton = new JButton("View All Comments");
                    commentsButton.addActionListener(e -> showAllComments(film));
                    card.add(commentsButton);
                }
            } else {
                JLabel noCommentsLabel = new JLabel("No comments available.");
                card.add(noCommentsLabel);
            }

            if (!SessionContext.isUserAdmin() && !SessionContext.isGuestUser()) {
                JButton addCommentButton = new JButton("Add Comment");
                addCommentButton.addActionListener(e -> addComment(film));
                card.add(createButtonPanel(addCommentButton));
            }
        } else {
            JLabel commentsDisabledLabel = new JLabel("Comments are disabled for this film.");
            card.add(commentsDisabledLabel);
        }

        JButton detailsButton = new JButton("Details");
        if (!SessionContext.isUserAdmin() && !SessionContext.isGuestUser()) {
            JButton addToCartButton = new JButton("Add to Cart");
            card.add(createButtonPanel(addToCartButton));
            addToCartButton.addActionListener(e -> addToCart(film));
        }
        card.add(createButtonPanel(detailsButton));

        detailsButton.addActionListener(e -> showFilmDetails(film));

        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        return card;
    }

    private String getPriceWithDiscount(String originalPrice) {
        double price = Double.parseDouble(originalPrice);
        if (SessionContext.isSubscribed()) {
            price -= 2;
        }
        return String.format("%.2f", price);
    }

    protected String renderStars(String ratingStr) {
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
        String commentText = JOptionPane.showInputDialog(frame, "Enter your comment:");
        if (commentText != null && !commentText.isEmpty()) {
            String rating = askForRating();
            String userId = SessionContext.getCurrentUserId();  // Récupère l'ID de l'utilisateur actuel

            boolean success = CSVManager.addCommentToFilmAndUser(film.getCode(), commentText, rating, userId);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Comment added successfully!");
                refreshFilmDisplay();  // Rafraîchit l'affichage pour montrer le nouveau commentaire
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to add comment.");
            }
        }
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

    // Afficher tous les commentaires
    protected void showAllComments(Film film) {
        JDialog commentsDialog = new JDialog(frame, "All Comments for " + film.getTitle(), true);
        JPanel commentPanel = new JPanel();
        commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));

        List<Comment> comments = new ArrayList<>(film.getCommentsForFilm(film.getCode()));

        // Buttons for sorting and filtering
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton sortButton = new JButton("Sort by Best Rated");
        JButton positiveCommentsButton = new JButton("Positive Comments (3+ stars)");
        JButton negativeCommentsButton = new JButton("Negative Comments (2- stars)");

        buttonsPanel.add(sortButton);
        buttonsPanel.add(positiveCommentsButton);
        buttonsPanel.add(negativeCommentsButton);
        commentsDialog.add(buttonsPanel, BorderLayout.NORTH);

        sortButton.addActionListener(e -> {
            comments.sort((c1, c2) -> Integer.compare(Integer.parseInt(c2.getRating()), Integer.parseInt(c1.getRating())));
            updateCommentPanel(comments, commentPanel);
        });

        positiveCommentsButton.addActionListener(e -> {
            List<Comment> filtered = comments.stream().filter(c -> Integer.parseInt(c.getRating()) >= 3).collect(Collectors.toList());
            updateCommentPanel(filtered, commentPanel);
        });

        negativeCommentsButton.addActionListener(e -> {
            List<Comment> filtered = comments.stream().filter(c -> Integer.parseInt(c.getRating()) <= 2).collect(Collectors.toList());
            updateCommentPanel(filtered, commentPanel);
        });

        // Initial comment panel setup
        updateCommentPanel(comments, commentPanel);

        JScrollPane scrollPane = new JScrollPane(commentPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        commentsDialog.add(scrollPane, BorderLayout.CENTER);
        commentsDialog.pack();
        commentsDialog.setLocationRelativeTo(frame);
        commentsDialog.setVisible(true);
    }

    protected void updateCommentPanel(List<Comment> comments, JPanel commentPanel) {
        commentPanel.removeAll();
        for (Comment comment : comments) {
            String userName = comment.getUserNameFromUserId(comment.getUsercode());
            JLabel commentLabel = new JLabel("<html><div style='width:200px;'><strong>" + userName + "</strong>: " + renderStars(comment.getRating()) +
                    "<br/>" + comment.getText() + "</div></html>");
            commentPanel.add(commentLabel);
        }
        commentPanel.revalidate();
        commentPanel.repaint();
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
        CSVManager.updateViews(film.getCode());  // Increment the view count
        JDialog detailsDialog = new JDialog(frame, "Film Details", true);
        detailsDialog.setLayout(new BorderLayout());

        JLabel imageLabel = createImageLabel(film.getImageURL());
        detailsDialog.add(imageLabel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        detailsPanel.add(new JLabel("Title: " + film.getTitle()));
        detailsPanel.add(new JLabel("Year: " + film.getProductionYear()));
        detailsPanel.add(new JLabel("Theme(s): " + String.join(", ", film.getTheme())));
        detailsPanel.add(new JLabel("Director: " + String.join(", ", film.getDirector())));
        detailsPanel.add(new JLabel("Actors: " + String.join(", ", film.getMainactors())));
        detailsPanel.add(new JLabel("Country: " + film.getCountry()));
        detailsPanel.add(new JLabel("Producer(s): " + String.join(", ", film.getProducers())));
        detailsPanel.add(new JLabel("Price: " + film.getPrice() + " euros "));
        detailsPanel.add(new JLabel("Duration: " + film.getDurationMinutes()));
        detailsPanel.add(new JLabel("Description: " + film.getDescription()));

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

        // Appel de la méthode pour charger ou recharger les films depuis le CSV
        List<Film> updatedFilms = filmManager.loadFilms();

        for (Film film : updatedFilms) {
            commentsEnabledMap.putIfAbsent(film.getCode(), true);  // Ensure every film has an entry
            filmPanel.add(createFilmCard(film));
        }
        filmPanel.revalidate();
        filmPanel.repaint();
    }

    protected double calculateAverageRating(List<Comment> comments) {
        if (comments.isEmpty()) {
            return 0.0; // No ratings available
        }
        double sum = 0.0;
        for (Comment comment : comments) {
            try {
                sum += Double.parseDouble(comment.getRating());
            } catch (NumberFormatException e) {
                System.err.println("Error parsing rating: " + comment.getRating());
            }
        }
        return sum / comments.size();
    }

    private void addToCart(Film film) {
        String userId = SessionContext.getCurrentUserId();
        if (userId != null) {
            try {
                if (CartManager.addToCart(userId, film.getCode())) {
                    JOptionPane.showMessageDialog(frame, "Added to cart successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Film is already in your cart.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to add to cart.");
            }
        }
    }

    private void viewCart() {
        String userId = SessionContext.getCurrentUserId();
        if (userId != null) {
            try {
                Set<String> filmIds = CartManager.loadCart().get(userId);
                if (filmIds != null && !filmIds.isEmpty()) {
                    new CartDisplay(filmIds, filmManager).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(frame, "Your cart is empty.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to load cart.");
            }
        }
    }

    private void openLoginWindow() {
        // Open the login window
        new LoginWindow(filmManager);
        frame.dispose();
    }

    private void openAccountPage() {
        // Open the account management interface
        new AccountManagementDialog(frame, SessionContext.getCurrentUser()).setVisible(true);
    }
}
