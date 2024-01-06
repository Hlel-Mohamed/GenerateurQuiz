package net.universitecentrale.generateurquiz.entity;

import java.util.List;

public class QuestionMCQ extends Question{
    private List<Option> options;

    public QuestionMCQ(Long id, String texte, Sujet sujet, List<Option> options) {
        super(id, texte, sujet);
        this.options = options;
    }

    public QuestionMCQ(String texte, Sujet sujet, List<Option> options) {
        super(texte, sujet);
        this.options = options;
    }

    public QuestionMCQ() {
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }
}
