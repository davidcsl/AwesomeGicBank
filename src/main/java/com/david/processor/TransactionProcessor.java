package com.david.processor;

import com.david.model.TransactionDetail;

import java.io.Console;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Vector;

public class TransactionProcessor {
  public static void inputTransaction(Vector<TransactionDetail> transactionHistory) {
    Console console = System.console();
    String transactionRequest = console.readLine(
            "Please enter transaction details in <Date> <Account> <Type> <Amount> format \n" +
                    "(or enter blank to go back to main menu):");

    if ("".equalsIgnoreCase(transactionRequest)) {
      return;
    }
    String[] payloads = transactionRequest.split(" ");

    try {
      transactionPayloadValidation(payloads);
      String transactionDate = payloads[0];
      String accountId = payloads[1];
      String type = payloads[2];
      Double amount = Double.parseDouble(payloads[3]);

      TransactionDetail previousTxn = null;
      Double lastBalance = 0D;
      String lastSameDayTxnId = null;
      if (transactionHistory.size() > 0) {

        previousTxn = transactionHistory.lastElement();
        lastBalance = Double.parseDouble(previousTxn.getBalance());

        if (Integer.parseInt(transactionDate) < Integer.parseInt(previousTxn.getDate())) {
          throw new Exception("Transaction input date is earlier than previous transaction.");
        }
        if (!accountId.equalsIgnoreCase(previousTxn.getAccountId())) {
          throw new Exception("Invalid transaction input account id.");
        }

        if (transactionDate.equalsIgnoreCase(previousTxn.getDate())
                && !"I".equalsIgnoreCase(previousTxn.getType())) {
          lastSameDayTxnId = previousTxn.getTransactionId();
        }
      }


      String newTxnId;
      StringBuilder stringBuilder = new StringBuilder();
      if (Objects.nonNull(previousTxn) && Objects.nonNull(lastSameDayTxnId)) {
        String previousTxnVersion = lastSameDayTxnId.substring(9);
        String newTxnVersion = String.format("%02d", Integer.parseInt(previousTxnVersion) + 1);
        stringBuilder.append(transactionDate);
        stringBuilder.append("-");
        stringBuilder.append(newTxnVersion);
        newTxnId = stringBuilder.toString();
      } else {
        stringBuilder.append(transactionDate);
        stringBuilder.append("-");
        stringBuilder.append("01");
        newTxnId = stringBuilder.toString();
      }

      if ("D".equalsIgnoreCase(type)) {
        Double newBalance = lastBalance + amount;

        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setAccountId(accountId);
        transactionDetail.setDate(transactionDate);
        transactionDetail.setTransactionId(newTxnId);
        transactionDetail.setType(type);
        transactionDetail.setAmount(amount.toString());
        transactionDetail.setBalance(newBalance.toString());
        transactionHistory.add(transactionDetail);

      } else if ("W".equalsIgnoreCase(type)) {
        Double newBalance = lastBalance - amount;

        if (newBalance < 0 ) {
          throw new Exception("Insufficient balance for withdrawal.");
        }

        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setAccountId(accountId);
        transactionDetail.setDate(transactionDate);
        transactionDetail.setTransactionId(newTxnId);
        transactionDetail.setType(type);
        transactionDetail.setAmount(amount.toString());
        transactionDetail.setBalance(newBalance.toString());
        transactionHistory.add(transactionDetail);
      }
      System.out.println("Account: " + accountId);

      System.out.println("| Date     | Txn Id      | Type | Amount |");

      transactionHistory.forEach(
              a -> System.out.println("| " + a.getDate()
                      + " | " + a.getTransactionId() + " | " + a.getType()
                      + "    | " + a.getAmount() + "   |"));

      System.out.println("Is there anything else you'd like to do?");
    } catch (DateTimeParseException e) {
      System.out.println("Invalid transaction payload found: Invalid date format input. " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Invalid transaction payload: " + e.getMessage());
    }

  }

  public static void transactionPayloadValidation(String[] payloads) throws Exception {

    if (payloads.length != 4) {
      throw new Exception("Invalid payload or one of the info is missing: <Date> <Account> <Type> <Amount>");
    }

    LocalDate date = LocalDate.parse(payloads[0], DateTimeFormatter.BASIC_ISO_DATE);
    String accountId = payloads[1];
    String type = payloads[2];
    Double amount = Double.parseDouble(payloads[3]);

    if (Objects.isNull(accountId)) {
      throw new Exception("Account id is null.");
    } else if (Objects.isNull(type)) {
      throw new Exception("Type is null.");
    } else if (!"D".equalsIgnoreCase(type) && !"W".equalsIgnoreCase(type)) {
      throw new Exception("Invalid transaction type.");
    } else if (amount < 0) {
      throw new Exception("Negative transaction amount detected.");
    }
  }
}
