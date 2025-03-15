package com.david;

import com.david.model.InterestRateDetail;
import com.david.model.TransactionDetail;
import com.david.processor.GeneralProcessor;
import java.util.Vector;

public class Main {
  public static void main(String[] args) throws Exception {

    System.out.println("Welcome to AwesomeGIC Bank! What would you like to do?");

    boolean isLoopOn = true;
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    while (isLoopOn) {
      String action = GeneralProcessor.displayActions();
      System.out.println("Selected action: " + action);
      isLoopOn = GeneralProcessor.processAction(action, transactionHistory, interestRateHistory);
    }

  }
}