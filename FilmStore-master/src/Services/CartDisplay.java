package Services;

import Entities.Film;
import Entities.User;
import Manager.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        setSize(500, 600);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        double totalPrice = 0;

        if (filmManager != null) {
            for (String filmId : filmIds) {
                Film film = filmManager.getFilmById(filmId);
                if (film != null) {
                    mainPanel.add(createFilmPanel(film));
                    totalPrice += Double.parseDouble(getPriceWithDiscount(film.getPrice()));
                } else {
                    System.out.println("Film not found: " + filmId);
                }
            }
        }

        JLabel totalLabel = new JLabel("Total: $" + String.format("%.2f", totalPrice));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(totalLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton("Clear Cart");
        clearButton.addActionListener(e -> clearCart());
        JButton buyButton = new JButton("Buy All");
        double finalTotalPrice = totalPrice;
        buyButton.addActionListener(e -> buyAll(finalTotalPrice));
        JButton subscribeButton = new JButton(SessionContext.isSubscribed() ? "Se Désabonner" : "S'abonner");
        subscribeButton.addActionListener(e -> toggleSubscription(subscribeButton));

        buttonPanel.add(clearButton);
        buttonPanel.add(buyButton);
        buttonPanel.add(subscribeButton);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createFilmPanel(Film film) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(film.getTitle() + " - $" + getPriceWithDiscount(film.getPrice()));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(titleLabel, BorderLayout.CENTER);

        try {
            URL url = new URL(film.getImageURL());
            ImageIcon imageIcon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(50, 75, Image.SCALE_SMOOTH));
            JLabel imageLabel = new JLabel(imageIcon);
            panel.add(imageLabel, BorderLayout.WEST);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> removeFromCart(film.getCode()));
        panel.add(removeButton, BorderLayout.EAST);

        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return panel;
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
        initializeUI();
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

    private void buyAll(double totalPrice) {
        try {
            User currentUser = SessionContext.getCurrentUser();
            for (String filmId : filmIds) {
                currentUser.addPurchase(filmId);
            }
            CSVManager.savePurchaseHistory(currentUser);
            generateInvoice(totalPrice);
            JOptionPane.showMessageDialog(this, "Purchase completed successfully!");
            clearCart(); // Clear the cart after purchase
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to complete purchase.");
        }
    }
    private void generateInvoice(double totalPrice) {
        String userId = SessionContext.getCurrentUserId();
        String userName = CSVManager.getUserNameFromUserId(userId);
        String invoiceDirPath = "./FilmStore-master/src/Invoices/";
        String invoiceFileName = "invoice_" + userId + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
        Path invoicePath = Paths.get(invoiceDirPath, invoiceFileName);

        try {
            Files.createDirectories(Paths.get(invoiceDirPath));
            try (BufferedWriter writer = Files.newBufferedWriter(invoicePath)) {
                writer.write("Invoice\n");
                writer.write("User : " + userName + "\n");
                writer.write("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                writer.write("\nPurchased Films:\n");

                for (String filmId : filmIds) {
                    Film film = filmManager.getFilmById(filmId);
                    if (film != null) {
                        writer.write("- " + film.getTitle() + " - $" + getPriceWithDiscount(film.getPrice()) + "\n");
                    }
                }

                writer.write("\nTotal: $" + String.format("%.2f", totalPrice) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
