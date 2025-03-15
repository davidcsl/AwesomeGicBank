package com.david.processor;

import com.david.model.InterestRateDetail;
import com.david.model.TransactionDetail;

import java.io.Console;
import java.util.Vector;

public class GeneralProcessor {

  public static String displayActions() {
    Console console = System.console();
    if (console == null) {
      System.out.println("Console is null");
      return "console is null";
    }

    String action = console.readLine(
            "[T] Input transactions \n" +
                    "[I] Define interest rules \n" +
                    "[P] Print statement \n" +
                    "[Q] Quit\n");
    return action;
  }

  public static boolean processAction(String action,
                                      Vector<TransactionDetail> transactionHistory,
                                      Vector<InterestRateDetail> interestRateHistory) {
    if ("T".equalsIgnoreCase(action)) {
      TransactionProcessor.inputTransaction(transactionHistory);
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
    System.out.println("Thank you for banking with AwesomeGIC Bank. \nHave a nice day!");
  }
}
