package Services;


import Entities.*;
import Manager.*;
import javax.swing.*;
import java.awt.*;
import java.text.ParseException;


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





}
