package Services;

import Entities.Film;
import Manager.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class CartDisplay extends JFrame {
    private Set<String> filmIds;
    private FilmManager filmManager;

    // Modified constructor to accept FilmManager instance
    public CartDisplay(Set<String> filmIds, FilmManager filmManager) {
        this.filmIds = filmIds;
        this.filmManager = filmManager;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Your Cart");
        setSize(300, 400);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        if (filmManager != null) {
            filmIds.forEach(filmId -> {
                Film film = filmManager.getFilmById(filmId);
                if (film != null) {
                    addFilmPanel(film);
                } else {
                    System.out.println("Film not found: " + filmId);
                }
            });
        }

        JButton clearButton = new JButton("Clear Cart");
        clearButton.addActionListener(e -> clearCart());
        JButton buyButton = new JButton("Buy All");
        buyButton.addActionListener(e -> buyAll());
        JButton subscribeButton = new JButton(SessionContext.isSubscribed() ? "Se Désabonner" : "S'abonner");
        subscribeButton.addActionListener(e -> toggleSubscription(subscribeButton));

        add(clearButton);
        add(buyButton);
        add(subscribeButton);
    }

    private void addFilmPanel(Film film) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel(film.getTitle() + " - $" + getPriceWithDiscount(film.getPrice()));
        ImageIcon imageIcon;
        try {
            imageIcon = new ImageIcon(new ImageIcon(new URL(film.getImageURL())).getImage().getScaledInstance(50, 75, Image.SCALE_SMOOTH));
            JLabel imageLabel = new JLabel(imageIcon);
            panel.add(imageLabel);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> removeFromCart(film.getCode()));
        panel.add(titleLabel);
        panel.add(removeButton);
        add(panel);
    }

    private String getPriceWithDiscount(String originalPrice) {
        double price = Double.parseDouble(originalPrice);
        if (SessionContext.isSubscribed()) {
            price -= 2;
        }
        return String.format("%.2f", price);
    }

    private void removeFromCart(String filmId) {
        String userId = SessionContext.getCurrentUserId();
        try {
            if (CartManager.removeFromCart(userId, filmId)) {
                JOptionPane.showMessageDialog(this, "Removed from cart successfully!");
                refreshCartDisplay(userId);  // Refresh the display
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove from cart.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshCartDisplay(String userId) throws IOException {
        Set<String> updatedFilmIds = CartManager.loadCart().get(userId);
        getContentPane().removeAll(); // Remove all UI components from the content pane
        if (updatedFilmIds != null) {
            updatedFilmIds.forEach(filmId -> {
                Film film = filmManager.getFilmById(filmId);
                if (film != null) {
                    addFilmPanel(film);
                } else {
                    System.out.println("Film not found: " + filmId);
                }
            });
        } else {
            System.out.println("Your cart is empty.");
        }
        JButton clearButton = new JButton("Clear Cart");
        clearButton.addActionListener(e -> clearCart());
        JButton buyButton = new JButton("Buy All");
        buyButton.addActionListener(e -> buyAll());
        JButton subscribeButton = new JButton(SessionContext.isSubscribed() ? "Se Désabonner" : "S'abonner");
        subscribeButton.addActionListener(e -> toggleSubscription(subscribeButton));
        add(clearButton);
        add(buyButton);
        add(subscribeButton);
        revalidate();
        repaint();
    }

    private void toggleSubscription(JButton subscribeButton) {
        boolean currentSubscriptionStatus = SessionContext.isSubscribed();
        SessionContext.setSubscribed(!currentSubscriptionStatus);
        CSVManager.setUserSubscribed(SessionContext.getCurrentUserId(), !currentSubscriptionStatus);
        subscribeButton.setText(!currentSubscriptionStatus ? "Se Désabonner" : "S'abonner");
        try {
            refreshCartDisplay(SessionContext.getCurrentUserId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearCart() {
        String userId = SessionContext.getCurrentUserId();
        try {
            CartManager.clearCart(userId);
            JOptionPane.showMessageDialog(this, "Cart cleared successfully!");
            this.dispose();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to clear cart.");
        }
    }

    private void buyAll() {
        // Implement purchasing logic
    }
}
