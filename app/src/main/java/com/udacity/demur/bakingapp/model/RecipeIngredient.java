package com.udacity.demur.bakingapp.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RecipeIngredient implements Serializable {
    @Expose
    @SerializedName("ingredient")
    private String ingredient;
    @Expose
    @SerializedName("measure")
    private String measure;
    @Expose
    @SerializedName("quantity")
    private double quantity;

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}