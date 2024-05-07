package Services;

import Manager.CartManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Set;

public class CartDisplay extends JFrame {
    private Set<String> filmIds;

    public CartDisplay(Set<String> filmIds) {
        this.filmIds = filmIds;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Your Cart");
        setSize(300, 400);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        filmIds.forEach(filmId -> {
            JPanel panel = new JPanel(new FlowLayout());
            JLabel label = new JLabel(filmId);  // You might want to fetch more details about the film to show here
            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(e -> removeFromCart(filmId));
            panel.add(label);
            panel.add(removeButton);
            add(panel);
        });

        JButton clearButton = new JButton("Clear Cart");
        clearButton.addActionListener(e -> clearCart());
        JButton buyButton = new JButton("Buy All");
        buyButton.addActionListener(e -> buyAll());

        add(clearButton);
        add(buyButton);
    }

    private void removeFromCart(String filmId) {
        String userId = SessionContext.getCurrentUserId();
        try {
            if (CartManager.removeFromCart(userId, filmId)) {
                JOptionPane.showMessageDialog(this, "Removed from cart successfully!");
                this.dispose();  // Close the window or update UI
                new CartDisplay(CartManager.loadCart().get(userId)).setVisible(true);  // Reopen or refresh the cart display
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove from cart.");
            }
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

