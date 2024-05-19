package AI;

import java.io.*;
import java.util.*;

public class CommentClassifier {
    private Map<String, Double> positiveWords;
    private Map<String, Double> negativeWords;
    private double positiveProb;
    private double negativeProb;

    public CommentClassifier(String filmsFilePath, String usersFilePath) throws IOException {
        positiveWords = new HashMap<>();
        negativeWords = new HashMap<>();
        train(filmsFilePath, usersFilePath);
    }

    private void train(String filmsFilePath, String usersFilePath) throws IOException {
        int positiveCount = 0;
        int negativeCount = 0;
        int totalComments = 0;

        // Process film comments
        try (BufferedReader reader = new BufferedReader(new FileReader(filmsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 12) {
                    String[] comments = parts[12].split("\\|");
                    for (String commentEntry : comments) {
                        String[] commentParts = commentEntry.split(",");
                        if (commentParts.length == 3) {
                            String comment = commentParts[0];
                            String rating = commentParts[1];
                            totalComments++;
                            if (Integer.parseInt(rating) >= 3) {
                                positiveCount++;
                                processWords(comment, positiveWords);
                            } else {
                                negativeCount++;
                                processWords(comment, negativeWords);
                            }
                        }
                    }
                }
            }
        }

        // Process user comments
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 8) {
                    String[] comments = parts[8].split("\\|");
                    for (String commentEntry : comments) {
                        String[] commentParts = commentEntry.split(",");
                        if (commentParts.length == 3) {
                            String comment = commentParts[0];
                            String rating = commentParts[1];
                            totalComments++;
                            if (Integer.parseInt(rating) >= 3) {
                                positiveCount++;
                                processWords(comment, positiveWords);
                            } else {
                                negativeCount++;
                                processWords(comment, negativeWords);
                            }
                        }
                    }
                }
            }
        }

        positiveProb = (double) positiveCount / totalComments;
        negativeProb = (double) negativeCount / totalComments;

        normalizeProbabilities(positiveWords, positiveCount);
        normalizeProbabilities(negativeWords, negativeCount);
    }

    private void processWords(String comment, Map<String, Double> wordMap) {
        String[] words = comment.split("\\s+");
        for (String word : words) {
            word = word.toLowerCase();  // Convertir les mots en minuscules pour une correspondance cohérente
            wordMap.put(word, wordMap.getOrDefault(word, 0.0) + 1.0);
        }
    }

    private void normalizeProbabilities(Map<String, Double> wordMap, int totalCount) {
        for (Map.Entry<String, Double> entry : wordMap.entrySet()) {
            wordMap.put(entry.getKey(), entry.getValue() / totalCount);
        }
    }

    public String classify(String comment) {
        double positiveScore = Math.log(positiveProb);
        double negativeScore = Math.log(negativeProb);

        String[] words = comment.split("\\s+");
        for (String word : words) {
            word = word.toLowerCase();  // Convertir les mots en minuscules pour une correspondance cohérente
            positiveScore += Math.log(positiveWords.getOrDefault(word, 1.0 / (positiveWords.size() + 1)));
            negativeScore += Math.log(negativeWords.getOrDefault(word, 1.0 / (negativeWords.size() + 1)));
        }

        return positiveScore > negativeScore ? "positif" : "negatif";
    }

    public static void main(String[] args) {
        try {
            CommentClassifier classifier = new CommentClassifier("./FilmStore-master/src/CSVBase/Films.csv", "./FilmStore-master/src/CSVBase/users.csv");
            // Load and classify all comments
            List<String> allComments = loadAllComments("./FilmStore-master/src/CSVBase/Films.csv", "./FilmStore-master/src/CSVBase/users.csv");
            for (String comment : allComments) {
                String classification = classifier.classify(comment);
                System.out.println("Comment: " + comment + " => " + classification);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> loadAllComments(String filmsFilePath, String usersFilePath) throws IOException {
        List<String> allComments = new ArrayList<>();

        // Process film comments
        try (BufferedReader reader = new BufferedReader(new FileReader(filmsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 12) {
                    String[] comments = parts[12].split("\\|");
                    for (String commentEntry : comments) {
                        String[] commentParts = commentEntry.split(",");
                        if (commentParts.length == 3) {
                            allComments.add(commentParts[0]);
                        }
                    }
                }
            }
        }

        // Process user comments
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 8) {
                    String[] comments = parts[8].split("\\|");
                    for (String commentEntry : comments) {
                        String[] commentParts = commentEntry.split(",");
                        if (commentParts.length == 3) {
                            allComments.add(commentParts[0]);
                        }
                    }
                }
            }
        }

        return allComments;
    }
}
