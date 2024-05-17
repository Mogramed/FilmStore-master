package Services;

import Entities.Comment;
import Entities.User;
import Manager.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import java.awt.*;

public class AccountManagementDialog extends JDialog {
    private User user;
    private JLabel passwordField;
    private JButton showPasswordButton;

    public AccountManagementDialog(Frame owner, User user) {
        super(owner, "Account Management", true);
        this.user = user;
        setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addField(infoPanel, "User ID:", new JLabel(user.getId()));
        addField(infoPanel, "Name:", new JLabel(user.getFirstname() + " " + user.getLastname()));
        addField(infoPanel, "Email:", new JLabel(user.getEmail()));
        addField(infoPanel, "Address:", new JLabel(user.getAddress()));
        addField(infoPanel, "Phone number:", new JLabel(String.valueOf(user.getPhonenumber())));


        passwordField = new JLabel(user.getPassword());
        passwordField.setText("********");
        addField(infoPanel, "Password:", passwordField);

        showPasswordButton = new JButton("Show Password");
        showPasswordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                verifyPassword();
            }
        });
        infoPanel.add(showPasswordButton);

        add(infoPanel, BorderLayout.NORTH);

        JPanel commentPanel = new JPanel();
        commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
        JScrollPane commentScroll = new JScrollPane(commentPanel);
        add(commentScroll, BorderLayout.CENTER);

        boolean isSubscribed = CSVManager.isUserSubscribed(user.getId());
        SessionContext.setSubscribed(isSubscribed);
        JButton subscribeButton = new JButton(isSubscribed ? "Se Désabonner" : "S'abonner");
        subscribeButton.addActionListener(e -> {
            boolean currentSubscriptionStatus = SessionContext.isSubscribed();
            SessionContext.setSubscribed(!currentSubscriptionStatus);
            CSVManager.setUserSubscribed(user.getId(), !currentSubscriptionStatus);
            subscribeButton.setText(!currentSubscriptionStatus ? "Se Désabonner" : "S'abonner");
        });
        infoPanel.add(subscribeButton);

        add(infoPanel, BorderLayout.NORTH);

        // Load comments associated with the user
        List<Comment> comments = CSVManager.getUserComments(user.getId());
        for (Comment comment : comments) {
            JPanel commentCard = new JPanel(new BorderLayout());
            commentCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    BorderFactory.createLineBorder(Color.GRAY)
            ));
            JLabel commentLabel = new JLabel("<html><div style='width:500px;'>" + comment.getFilmcode() + ": " + comment.getText() + "</div></html>");
            commentLabel.setVerticalAlignment(JLabel.TOP);
            commentCard.add(commentLabel, BorderLayout.CENTER);

            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton editButton = new JButton("Edit");
            editButton.addActionListener(e -> editComment(comment));
            buttonsPanel.add(editButton);

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> deleteComment(comment));
            buttonsPanel.add(deleteButton);

            commentCard.add(buttonsPanel, BorderLayout.SOUTH);
            commentPanel.add(commentCard);
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private void addField(JPanel panel, String labelText, JComponent field) {
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 5));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 20));
        fieldPanel.add(label, BorderLayout.WEST);
        fieldPanel.add(field, BorderLayout.CENTER);
        panel.add(fieldPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void editComment(Comment comment) {
        // Open a dialog to edit the comment
        JTextField commentField = new JTextField(comment.getText());
        JTextField ratingField = new JTextField(comment.getRating());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Comment:"));
        panel.add(commentField);
        panel.add(new JLabel("Rating:"));
        panel.add(ratingField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Comment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String newCommentText = commentField.getText().trim();
            String newRating = ratingField.getText().trim();

            if (!newCommentText.isEmpty() && !newRating.isEmpty()) {
                boolean success = CSVManager.editCommentInFilmAndUser(comment.getUsercode(), comment.getFilmcode(), comment.getRating(), comment.getText(), newRating, newCommentText);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Comment updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update comment.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Comment and rating cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void deleteComment(Comment comment) {
        // Delete the comment
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this comment?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            boolean success = CSVManager.removeCommentFromFilmAndUser(comment.getUsercode(), comment.getFilmcode(), comment.getRating(), comment.getText());
            System.out.println(comment.getFilmcode()+" et " + comment.getUsercode()+ comment.getRating()+ comment.getText());
            if (success) {
                JOptionPane.showMessageDialog(this, "Comment deleted.");
                dispose(); // Close the dialog
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete comment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void verifyPassword() {
        String enteredPassword = JOptionPane.showInputDialog(this, "Enter your password to confirm:");
        if (enteredPassword != null && enteredPassword.equals(SessionContext.getCurrentUser().getPassword())) {
            passwordField.setText(user.getPassword()); // Show password
            JOptionPane.showMessageDialog(this, "Password visibility enabled.");
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
