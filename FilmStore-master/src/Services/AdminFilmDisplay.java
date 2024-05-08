package Services;


import Entities.*;
import Manager.*;
import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AdminFilmDisplay extends FilmDisplay {
    private FilmManager filmManager;

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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(modifyButton);
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

}
