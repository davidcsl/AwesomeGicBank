package com.david.processor;

import com.david.model.TransactionDetail;

import java.io.Console;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class PrintStatementProcessor {

  public static void printStatement(Vector<TransactionDetail> transactionHistory) {
    Console console = System.console();
    String printStatementRequest = console.readLine(
            "Please enter account and month to generate the statement <Account> <Year><Month> \n" +
                    "(or enter blank to go back to main menu):");

    if ("".equalsIgnoreCase(printStatementRequest)) {
      return;
    }

    String[] payloads = printStatementRequest.split(" ");

    try {
      printStatementPayloadValidation(payloads);
      String accountId = payloads[0];
      String dateRange = payloads[1];

      List<TransactionDetail> transactionDetailList = new ArrayList<>();
      if (transactionHistory.size() > 0) {
        transactionDetailList = transactionHistory.stream()
                .filter(a -> a.getAccountId().equalsIgnoreCase(accountId))
                .filter(a -> a.getDate().substring(0, 6).equalsIgnoreCase(dateRange))
                .toList();
      }
      if (!transactionDetailList.isEmpty()) {
        System.out.println("\nAccount: " + transactionDetailList.get(0).getAccountId());
        System.out.printf("| %-10s | %-12s | %-10s | %10s | %10s | %n", "Date", "Txn Id", "Type", "Amount", "Balance");

        transactionDetailList.forEach(
                a -> System.out.printf("| %-10s | %-12s | %-10s | %10s | %10s | %n",
                        a.getDate(), a.getTransactionId(), a.getType(), a.getAmount(), a.getBalance()));

      } else {
        System.out.println("There is no transactions");
      }
      System.out.println("\nIs there anything else you'd like to do?");

    } catch (DateTimeParseException e) {
      System.out.println("Invalid Print Statement payload found: Invalid date format input. " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Invalid transaction payload: " + e.getMessage());
    }
  }

  private static void printStatementPayloadValidation(String[] payloads) throws Exception {
    if (payloads.length != 2) {
      throw new Exception("Invalid payload or one of the info is missing: <Account> <Year><Month>");
    }

    String accountId = payloads[0];
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMM");
    YearMonth yearMonth = YearMonth.parse(payloads[1], formatter);

    if (Objects.isNull(accountId)) {
      throw new Exception("Account id is null.");
    }
  }
}
