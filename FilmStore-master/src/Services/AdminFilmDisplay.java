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

        JButton deleteButton = new JButton("Supprimer");
        deleteButton.addActionListener(e -> deleteFilm(film));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(modifyButton);
        buttonPanel.add(deleteButton);

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


    private void deleteFilm(Film film) {
        // Demander confirmation avant de supprimer un film
        int response = JOptionPane.showConfirmDialog(null, "Voulez-vous vraiment supprimer le film : " + film.getTitle() + " ?", "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            filmManager.deleteFilm(film.getCode());
            refreshFilmDisplay();
        }
    }





}
