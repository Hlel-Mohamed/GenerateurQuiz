package net.universitecentrale.generateurquiz.entity;

public class QuestionRemplirBlanc extends Question{
    private String reponse;

    public QuestionRemplirBlanc(Long id, String texte, Sujet sujet, String reponse) {
        super(id, texte, sujet);
        this.reponse = reponse;
    }

    public QuestionRemplirBlanc(String texte, Sujet sujet, String reponse) {
        super(texte, sujet);
        this.reponse = reponse;
    }

    public QuestionRemplirBlanc() {
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }
}
