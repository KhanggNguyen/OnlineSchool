package com.example.onlineschool.Models;

public class Formula {
    private String documentID;
    private String price;
    private String formula_name;

    public Formula() {
    }

    public Formula(String price, String formula_name) {
        this.price = price;
        this.formula_name = formula_name;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getFormula_name() {
        return formula_name;
    }

    public void setFormula_name(String formula_name) {
        this.formula_name = formula_name;
    }
}
