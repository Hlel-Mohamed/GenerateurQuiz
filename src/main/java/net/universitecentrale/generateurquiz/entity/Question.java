package net.universitecentrale.generateurquiz.entity;

import java.util.List;

public class Question {
    private Long id;
    private String texte;
    private Sujet sujet;

    public Question(Long id, String texte, Sujet sujet) {
        this.id = id;
        this.texte = texte;
        this.sujet = sujet;
    }

    public Question(String texte, Sujet sujet) {
        this.texte = texte;
        this.sujet = sujet;
    }

    public Question() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public Sujet getSujet() {
        return sujet;
    }

    public void setSujet(Sujet sujet) {
        this.sujet = sujet;
    }
}
