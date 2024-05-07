package Services;


import Entities.*;
import Manager.*;
import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


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
        JPanel commentPanel = new JPanel();
        commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));

        List<Comment> comments = new ArrayList<>(film.getCommentsForFilm(film.getCode()));
        for (Comment comment : comments) {
            JPanel singleCommentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel commentLabel = new JLabel("<html><strong>" + comment.getUsercode() + "</strong>: " + renderStars(comment.getRating()) +
                    "<br/>" + comment.getText() + "</html>");
            singleCommentPanel.add(commentLabel);

            // Add a delete button for each comment if the user is an admin
            JButton deleteCommentButton = new JButton("Delete Comment");
            deleteCommentButton.addActionListener(e -> {
                removeComment(film, comment.getUsercode(), comment.getText(), comment.getRating());
                commentsDialog.dispose(); // Close the dialog to refresh after deletion
                showAllComments(film); // Reopen the comments dialog to show updated list
            });
            singleCommentPanel.add(deleteCommentButton);
            commentPanel.add(singleCommentPanel);
        }

        JScrollPane scrollPane = new JScrollPane(commentPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        commentsDialog.add(scrollPane, BorderLayout.CENTER);
        commentsDialog.pack();
        commentsDialog.setLocationRelativeTo(frame);
        commentsDialog.setVisible(true);
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
