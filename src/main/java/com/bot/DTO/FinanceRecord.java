package com.bot.DTO;

public class FinanceRecord {
    private String date;
    private double amount;


    public FinanceRecord() {
    }

    public FinanceRecord(String date, double amount) {
        this.date = date;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "FinanceRecord{" +
                "Дата = '" + date + '\'' +
                ", Сумма = " + amount +
                '}';
    }
}
