package com.example.onlineschool.Models;

import com.google.firebase.Timestamp;

public class Subscription {
    private Formula formula_choisi;
    private Timestamp date_fin;

    public Subscription() {
    }

    public Subscription(Formula formula_choisi, Timestamp date_fin) {
        this.formula_choisi = formula_choisi;
        this.date_fin = date_fin;
    }

    public Formula getFormula_choisi() {
        return formula_choisi;
    }

    public void setFormula_choisi(Formula formula_choisi) {
        this.formula_choisi = formula_choisi;
    }

    public Timestamp getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(Timestamp date_fin) {
        this.date_fin = date_fin;
    }
}
