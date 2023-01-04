package model;

import java.io.Serializable;

public class Transaction implements Serializable {
    private String accountNumber;
    private double transactionAmount;
    private String transactionType;
    private double accountBalance;

    //Default Constructor
    public Transaction() {
        this.accountNumber = "0000000";
        this.transactionAmount = -1;
        this.transactionType = "N/A";
        this.accountBalance = -1.0;
    }

    //Primary Constructor 1
    public Transaction(String accountNumber, double transactionAmount, String transactionType, double accountBalance) {
        this.accountNumber = accountNumber;
        this.transactionAmount = transactionAmount;
        this.transactionType = transactionType;
        this.accountBalance = accountBalance;
    }

    //Primary Constructor 2
    public Transaction(String accountNumber, double transactionAmount, String transactionType) {
        this.accountNumber = accountNumber;
        this.transactionAmount = transactionAmount;
        this.transactionType = transactionType;
        this.accountBalance = 0.0;
    }


    //Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionType() {
        return transactionType;
    }


    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    @Override
    public String toString() {
        return "Transaction [getAccountNumber()=" + getAccountNumber() + ", getTransactionAmount()="
                + getTransactionAmount() + ", getTransactionType()=" + getTransactionType() + ", getAccountBalance()="
                + getAccountBalance() + "]";
    }

}
