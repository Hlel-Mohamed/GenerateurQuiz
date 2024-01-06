package net.universitecentrale.generateurquiz.entity;

public class Option {
    private Long id;
    private String texte;
    private boolean correcte;
    private QuestionMCQ question;

    public Option(Long id, String texte, boolean correcte, QuestionMCQ question) {
        this.id = id;
        this.texte = texte;
        this.correcte = correcte;
        this.question = question;
    }

    public Option(String texte, boolean correcte, QuestionMCQ question) {
        this.texte = texte;
        this.correcte = correcte;
        this.question = question;
    }

    public Option() {}

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

    public boolean isCorrecte() {
        return correcte;
    }

    public void setCorrecte(boolean correcte) {
        this.correcte = correcte;
    }

    public QuestionMCQ getQuestion() {
        return question;
    }

    public void setQuestion(QuestionMCQ question) {
        this.question = question;
    }
}
