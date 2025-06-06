package org.example.model;

// model/model.Transaction.java
public class Transaction {
    private int id;
    private String type; // "收入" 或 "支出"
    private String description;
    private double amount;
    private String date;

    public Transaction(String type, String description, double amount, String date) {
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }

    // Getter 和 Setter for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter 和 Setter for type
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Getter 和 Setter for description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter 和 Setter for amount
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Getter 和 Setter for date
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
