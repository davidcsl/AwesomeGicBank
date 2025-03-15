package com.david.model;

public class TransactionDetail {

  private String accountId;
  private String type;
  private String date;
  private String transactionId;
  private String amount;
  private String balance;

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public String getBalance() {
    return balance;
  }

  public void setBalance(String balance) {
    this.balance = balance;
  }

  @Override
  public String toString() {
    return "TransactionDetail{" +
            "accountId='" + accountId + '\'' +
            ", type='" + type + '\'' +
            ", date='" + date + '\'' +
            ", transactionId='" + transactionId + '\'' +
            ", amount='" + amount + '\'' +
            ", balance='" + balance + '\'' +
            '}';
  }
}
