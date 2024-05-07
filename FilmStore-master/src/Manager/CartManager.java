package Manager;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CartManager {
    private static final String CART_CSV_PATH = "./FilmStore-master/src/CSVBase/carts.csv";

    public static boolean addToCart(String userId, String filmId) throws IOException {
        Map<String, Set<String>> cart = loadCart();
        if (cart.containsKey(userId) && cart.get(userId).contains(filmId)) {
            return false; // Film already in cart
        }
        cart.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        saveCart(cart);
        return true;
    }

    public static boolean removeFromCart(String userId, String filmId) throws IOException {
        Map<String, Set<String>> cart = loadCart();
        if (cart.containsKey(userId) && cart.get(userId).remove(filmId)) {
            saveCart(cart);
            return true;
        }
        return false;
    }

    public static void clearCart(String userId) throws IOException {
        Map<String, Set<String>> cart = loadCart();
        cart.remove(userId);
        saveCart(cart);
    }

    public static Map<String, Set<String>> loadCart() throws IOException {
        Map<String, Set<String>> cart = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CART_CSV_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                String userId = parts[0];
                Set<String> films = Arrays.stream(parts[1].split("\\|")).collect(Collectors.toSet());
                cart.put(userId, films);
            }
        }
        return cart;
    }

    private static void saveCart(Map<String, Set<String>> cart) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CART_CSV_PATH))) {
            for (Map.Entry<String, Set<String>> entry : cart.entrySet()) {
                String films = String.join("|", entry.getValue());
                writer.println(entry.getKey() + ";" + films);
            }
        }
    }
}

