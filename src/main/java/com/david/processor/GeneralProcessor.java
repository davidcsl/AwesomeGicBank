package com.david.processor;

import com.david.model.InterestRateDetail;
import com.david.model.TransactionDetail;

import java.util.Scanner;
import java.util.Vector;

public class GeneralProcessor {

  public static String displayActions() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("[T] Input transactions \n" +
            "[I] Define interest rules \n" +
            "[P] Print statement \n" +
            "[Q] Quit\n");
    String action = scanner.nextLine();
    return action;
  }

  public static boolean processAction(String action,
                                      Vector<TransactionDetail> transactionHistory,
                                      Vector<InterestRateDetail> interestRateHistory) {
    if ("T".equalsIgnoreCase(action)) {
      TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
      return true;

    } else if ("I".equalsIgnoreCase(action)) {
      InterestRateProcessor.defineInterestRule(interestRateHistory);
      return true;

    } else if ("P".equalsIgnoreCase(action)) {
      PrintStatementProcessor.printStatement(transactionHistory);
      return true;

    } else if ("Q".equalsIgnoreCase(action)) {
      quit();
      return false;

    } else {
      System.out.println("Invalid input, please try again!");
      return true;
    }
  }

  public static void quit() {
    System.out.println("Thank you for banking with AwesomeGIC Bank. " +
            "\nHave a nice day!");
  }
}
