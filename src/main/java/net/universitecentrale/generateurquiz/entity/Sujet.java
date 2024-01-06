package net.universitecentrale.generateurquiz.entity;

import java.util.List;

public class Sujet {
    private Long id;
    private String texte;
    private List<Question> questions;

    public Sujet(Long id, String texte, List<Question> questions) {
        this.id = id;
        this.texte = texte;
        this.questions = questions;
    }

    public Sujet(String texte, List<Question> questions) {
        this.texte = texte;
        this.questions = questions;
    }

    public Sujet() {
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

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
