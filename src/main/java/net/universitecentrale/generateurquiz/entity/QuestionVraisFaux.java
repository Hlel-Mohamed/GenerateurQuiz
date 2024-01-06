package net.universitecentrale.generateurquiz.entity;

public class QuestionVraisFaux extends Question{
    private boolean reponseCorrecte;

    public QuestionVraisFaux(Long id, String texte, Sujet sujet, boolean reponseCorrecte) {
        super(id, texte, sujet);
        this.reponseCorrecte = reponseCorrecte;
    }

    public QuestionVraisFaux(String texte, Sujet sujet, boolean reponseCorrecte) {
        super(texte, sujet);
        this.reponseCorrecte = reponseCorrecte;
    }

    public QuestionVraisFaux() {
    }

    public boolean isReponseCorrecte() {
        return reponseCorrecte;
    }

    public void setReponseCorrecte(boolean reponseCorrecte) {
        this.reponseCorrecte = reponseCorrecte;
    }
}