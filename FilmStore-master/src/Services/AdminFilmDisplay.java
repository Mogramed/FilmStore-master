package Services;

import Entities.Comment;
import Entities.Film;
import Manager.CSVManager;
import Manager.CartManager;
import Manager.FilmDetailsForm;
import Manager.FilmManager;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AdminFilmDisplay extends FilmDisplay {
    private FilmManager filmManager;
    private static final String COMMENTS_STATE_FILE_PATH = "./FilmStore-master/src/CSVBase/commentsEnabledState.csv";


    public AdminFilmDisplay(FilmManager filmManager) {
        super(filmManager);
        this.filmManager = filmManager;
    }

    @Override
    protected JPanel createFilmCard(Film film) {
        JPanel card = super.createFilmCard(film);

        // Ajout de boutons spécifiques à l'administration pour chaque film
        JButton modifyButton = new JButton("Modifier");
        modifyButton.addActionListener(e -> modifyFilm(film));
        JButton toggleCommentsButton = new JButton(commentsEnabledMap.getOrDefault(film.getCode(), true) ? "Désactiver Commentaires" : "Activer Commentaires");
        toggleCommentsButton.addActionListener(e -> {
            boolean currentState = commentsEnabledMap.getOrDefault(film.getCode(), true);
            commentsEnabledMap.put(film.getCode(), !currentState);
            toggleCommentsButton.setText(!currentState ? "Désactiver Commentaires" : "Activer Commentaires");
            saveCommentsEnabledState();
            refreshFilmDisplay();
        });

        // Bouton pour générer les statistiques
        JButton statsButton = new JButton("Generate Statistics");
        statsButton.addActionListener(e -> generateStatistics(film));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(modifyButton);
        buttonPanel.add(toggleCommentsButton);
        buttonPanel.add(statsButton); // Ajouter le bouton de statistiques
        card.add(buttonPanel, BorderLayout.PAGE_END);

        return card;
    }


    private void modifyFilm(Film film) {
        FilmDetailsForm form = new FilmDetailsForm(film);
        JPanel formPanel = form.getFormPanel();
        int result = JOptionPane.showConfirmDialog(frame, formPanel, "Modifier Film", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Film updatedFilm = form.updateFilmFromForm();
                filmManager.updateFilm(film.getCode(), updatedFilm);
                refreshFilmDisplay();
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(frame, "Erreur de formatage de l'année.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void saveCommentsEnabledState() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COMMENTS_STATE_FILE_PATH))) {
            for (Map.Entry<String, Boolean> entry : commentsEnabledMap.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
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

    @Override
    protected void showAllComments(Film film) {
        JDialog commentsDialog = new JDialog(frame, "All Comments for " + film.getTitle(), true);
        commentsDialog.setSize(600, 400);
        JPanel commentPanel = new JPanel();
        commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(commentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(580, 350));

        commentsDialog.add(scrollPane, BorderLayout.CENTER);

        List<Comment> comments = new ArrayList<>(film.getCommentsForFilm(film.getCode()));
        for (Comment comment : comments) {
            JPanel singleCommentPanel = new JPanel();
            singleCommentPanel.setLayout(new BoxLayout(singleCommentPanel, BoxLayout.Y_AXIS));
            JLabel commentLabel = new JLabel("<html><div style='width:350px;'><strong>" + comment.getUserNameFromUserId(comment.getUsercode()) + "</strong>: " + renderStars(comment.getRating()) +
                    "<br/>" + comment.getText() + "</div></html>");  // Ensure the div width is set

            singleCommentPanel.add(commentLabel);
            JButton deleteCommentButton = new JButton("Delete Comment");
            deleteCommentButton.addActionListener(e -> {
                removeComment(film, comment.getUsercode(), comment.getText(), comment.getRating());
                commentsDialog.dispose();
                showAllComments(film);
            });
            singleCommentPanel.add(deleteCommentButton);
            commentPanel.add(singleCommentPanel);
        }

        // Initialize buttons and actions only once outside the loop
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton sortButton = new JButton("Sort by Best Rated");
        JButton positiveCommentsButton = new JButton("Positive Comments (3+ stars)");
        JButton negativeCommentsButton = new JButton("Negative Comments (2- stars)");

        buttonsPanel.add(sortButton);
        buttonsPanel.add(positiveCommentsButton);
        buttonsPanel.add(negativeCommentsButton);
        commentsDialog.add(buttonsPanel, BorderLayout.NORTH);

        setCommentSortingActions(sortButton, positiveCommentsButton, negativeCommentsButton, comments, commentPanel, film, commentsDialog);

        commentsDialog.setLocationRelativeTo(frame);
        commentsDialog.setVisible(true);
    }

    private void setCommentSortingActions(JButton sortButton, JButton positiveCommentsButton, JButton negativeCommentsButton, List<Comment> comments, JPanel commentPanel, Film film, JDialog commentsDialog) {
        sortButton.addActionListener(e -> {
            comments.sort((c1, c2) -> Integer.compare(Integer.parseInt(c2.getRating()), Integer.parseInt(c1.getRating())));
            refreshCommentsDisplay(comments, commentPanel, film, commentsDialog);
        });

        positiveCommentsButton.addActionListener(e -> {
            List<Comment> filtered = comments.stream().filter(c -> Integer.parseInt(c.getRating()) >= 3).collect(Collectors.toList());
            refreshCommentsDisplay(filtered, commentPanel, film, commentsDialog);
        });

        negativeCommentsButton.addActionListener(e -> {
            List<Comment> filtered = comments.stream().filter(c -> Integer.parseInt(c.getRating()) <= 2).collect(Collectors.toList());
            refreshCommentsDisplay(filtered, commentPanel, film, commentsDialog);
        });
    }

    private void refreshCommentsDisplay(List<Comment> comments, JPanel commentPanel, Film film, JDialog commentsDialog) {
        commentPanel.removeAll();  // Clear existing components
        for (Comment comment : comments) {
            JPanel singleCommentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel commentLabel = new JLabel("<html><div style='width:300px;'><strong>" + comment.getUserNameFromUserId(comment.getUsercode()) + "</strong>: " + renderStars(comment.getRating()) +
                    "<br/>" + comment.getText() + "</div></html>");
            singleCommentPanel.add(commentLabel);

            JButton deleteCommentButton = new JButton("Delete Comment");
            deleteCommentButton.addActionListener(e -> {
                removeComment(film, comment.getUsercode(), comment.getText(), comment.getRating());
                commentsDialog.dispose(); // Close the dialog to refresh after deletion
                showAllComments(film); // Reopen the comments dialog to show updated list
            });
            singleCommentPanel.add(deleteCommentButton);
            commentPanel.add(singleCommentPanel);
        }
        commentPanel.revalidate();
        commentPanel.repaint();
    }

    private void removeComment(Film film, String userId, String commentText, String rating) {
        boolean success = CSVManager.removeCommentFromFilmAndUser(film.getCode(), userId, rating, commentText);
        if (success) {
            JOptionPane.showMessageDialog(frame, "Comment removed successfully!");
            refreshFilmDisplay();  // Mise à jour de l'affichage pour refléter la suppression du commentaire
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to remove comment.");
        }
    }

    private void generateStatistics(Film film) {
        JDialog statsDialog = new JDialog(frame, "Statistics for " + film.getTitle(), true);
        statsDialog.setLayout(new BorderLayout());
        statsDialog.setSize(500, 400);
        statsDialog.setLocationRelativeTo(frame);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<Comment> comments = film.getCommentsForFilm(film.getCode());
        double averageRating = calculateAverageRating(comments);
        int numberOfPurchases = calculateNumberOfPurchases(film.getCode());
        int numberOfComments = comments.size();
        int positiveComments = countPositiveComments(comments);
        int negativeComments = countNegativeComments(comments);
        double totalRevenue = calculateTotalRevenue(film.getCode(), film.getPrice());
        int addedToCartCount = calculateAddedToCartCount(film.getCode());
        int numberOfViews = calculateNumberOfViews(film.getCode());  // hypothetical if you have this data
        double averageCommentLength = calculateAverageCommentLength(comments);
        double positiveCommentPercentage = calculatePositiveCommentPercentage(positiveComments, numberOfComments);

        addStatistic(statsPanel, "Average Rating", String.format("%.2f", averageRating));
        addStatistic(statsPanel, "Number of Purchases", String.valueOf(numberOfPurchases));
        addStatistic(statsPanel, "Number of Comments", String.valueOf(numberOfComments));
        addStatistic(statsPanel, "Positive Comments", String.valueOf(positiveComments));
        addStatistic(statsPanel, "Negative Comments", String.valueOf(negativeComments));
        addStatistic(statsPanel, "Total Revenue", String.format("$%.2f", totalRevenue));
        addStatistic(statsPanel, "Added to Cart", String.valueOf(addedToCartCount));
        addStatistic(statsPanel, "Number of Views", String.valueOf(numberOfViews));
        addStatistic(statsPanel, "Average Comment Length", String.format("%.2f words", averageCommentLength));
        addStatistic(statsPanel, "Positive Comment Percentage", String.format("%.2f%%", positiveCommentPercentage));

        statsDialog.add(statsPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> statsDialog.dispose());
        statsDialog.add(closeButton, BorderLayout.SOUTH);

        statsDialog.setVisible(true);
    }


    private int calculateNumberOfPurchases(String filmCode) {
        // Calculer le nombre d'achats pour ce film
        // Vous pouvez implémenter cette méthode en comptant les occurrences du film dans le panier des utilisateurs ou autre logique d'achat
        // Exemple de logique :
        int count = 0;
        try {
            Map<String, Set<String>> cart = CartManager.loadCart();
            for (Set<String> filmIds : cart.values()) {
                if (filmIds.contains(filmCode)) {
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    private int calculateNumberOfViews(String filmCode) {
        Map<String, Integer> viewsMap = CSVManager.loadViews();
        return viewsMap.getOrDefault(filmCode, 0);
    }

    private void addStatistic(JPanel panel, String label, String value) {
        JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statLabel = new JLabel(label + ": ");
        statLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel statValue = new JLabel(value);
        statValue.setFont(new Font("Arial", Font.PLAIN, 14));
        statPanel.add(statLabel);
        statPanel.add(statValue);
        panel.add(statPanel);
    }


    private int countPositiveComments(List<Comment> comments) {
        int count = 0;
        for (Comment comment : comments) {
            try {
                int rating = Integer.parseInt(comment.getRating());
                if (rating >= 3) {
                    count++;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    private int countNegativeComments(List<Comment> comments) {
        int count = 0;
        for (Comment comment : comments) {
            try {
                int rating = Integer.parseInt(comment.getRating());
                if (rating < 3) {
                    count++;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    private double calculateTotalRevenue(String filmCode, String priceStr) {
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0.0;
        }
        int numberOfPurchases = calculateNumberOfPurchases(filmCode);
        return price * numberOfPurchases;
    }

    private int calculateAddedToCartCount(String filmCode) {
        int count = 0;
        try {
            Map<String, Set<String>> cart = CartManager.loadCart();
            for (Set<String> filmIds : cart.values()) {
                if (filmIds.contains(filmCode)) {
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    private double calculateAverageCommentLength(List<Comment> comments) {
        int totalWords = 0;
        for (Comment comment : comments) {
            totalWords += comment.getText().split("\\s+").length;
        }
        return comments.isEmpty() ? 0.0 : (double) totalWords / comments.size();
    }

    private double calculatePositiveCommentPercentage(int positiveComments, int totalComments) {
        return totalComments == 0 ? 0.0 : (double) positiveComments / totalComments * 100;
    }


}
