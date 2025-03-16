package com.david.processor;

import com.david.model.InterestRateDetail;
import com.david.model.TransactionDetail;

import java.io.Console;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class TransactionProcessor {
  public static void inputTransaction(Vector<TransactionDetail> transactionHistory,
                                      Vector<InterestRateDetail> interestRateHistory) {
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

      Double newBalance = null;
      if ("D".equalsIgnoreCase(type)) {
        newBalance = lastBalance + amount;

      } else if ("W".equalsIgnoreCase(type)) {
        newBalance = lastBalance - amount;
        if (newBalance < 0 ) {
          throw new Exception("Insufficient balance for withdrawal.");
        }
      }
      if (Objects.isNull(newBalance)) {
        throw new Exception("Invalid transaction operation as new balance is invalid / null.");
      }

      TransactionDetail transactionDetail = new TransactionDetail();
      transactionDetail.setAccountId(accountId);
      transactionDetail.setDate(transactionDate);
      transactionDetail.setTransactionId(newTxnId);
      transactionDetail.setType(type);
      transactionDetail.setAmount(amount.toString());
      transactionDetail.setBalance(newBalance.toString());


      if (transactionHistory.size() > 0) {
        Integer monthDifference =
                Integer.parseInt(transactionDate.substring(0, 6))
                        - Integer.parseInt(previousTxn.getDate().substring(0, 6));
        if (monthDifference > 0) {
          interestPayout(transactionHistory, monthDifference, interestRateHistory);
        }
      }
      transactionHistory.add(transactionDetail);
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
      System.out.println("Invalid transaction payload: " + e.getStackTrace().toString());
      e.printStackTrace();
    }

  }

  private static void interestPayout(Vector<TransactionDetail> transactionHistory,
                                     Integer monthDifference,
                                     Vector<InterestRateDetail> interestRateHistory) {

    String interestCalcYearMonth = transactionHistory.lastElement().getDate().substring(0, 6);

    List<TransactionDetail> interestMonthTransaction = transactionHistory
            .stream()
            .filter(a -> interestCalcYearMonth.equalsIgnoreCase(a.getDate().substring(0, 6)))
            .toList();
    List<InterestRateDetail> interestMonthInterest = interestRateHistory
            .stream()
            .filter(a -> interestCalcYearMonth.equalsIgnoreCase(a.getDate().substring(0, 6)))
            .toList();

    Double currentInterestRate = 1D;
    if (interestRateHistory.size() > 0)  {
      String extractedInitialRate = interestRateHistory
              .stream()
              .filter(a -> Integer.parseInt(a.getDate().substring(0, 6)) < Integer.parseInt(interestCalcYearMonth))
              .sorted((s1, s2) -> Integer.compare(Integer.parseInt(s2.getDate()), Integer.parseInt(s1.getDate())))
              .map(InterestRateDetail::getRate)
              .findFirst()
              .orElse(null);
      if (Objects.nonNull(extractedInitialRate)) {
        currentInterestRate = Double.parseDouble(extractedInitialRate);
      }
    }

    double annualizedInterest = 0D;
    Integer dateFrom = 1;
    for (int i = 0; i < interestMonthTransaction.size(); i++) {

      if (i + 1 < interestMonthTransaction.size() &&
              interestMonthTransaction.get(i).getDate()
                      .equals(interestMonthTransaction.get(i+1).getDate())) {
        continue;
      }

      TransactionDetail transactionDetail = interestMonthTransaction.get(i);
      Double eodBalancePrior = null;
      if ("D".equalsIgnoreCase(transactionDetail.getType())) {
        eodBalancePrior = Double.parseDouble(transactionDetail.getBalance()) -
                Double.parseDouble(transactionDetail.getAmount());
      } else if ("W".equalsIgnoreCase(transactionDetail.getType())) {
        eodBalancePrior = Double.parseDouble(transactionDetail.getBalance()) +
                Double.parseDouble(transactionDetail.getAmount());
      }

      if (Objects.nonNull(eodBalancePrior)) {
        int dateTo = Integer.parseInt(transactionDetail.getDate().substring(6));
        int finalDateFrom = dateFrom;
        List<InterestRateDetail> interestMonthInterestPortion = interestMonthInterest
                .stream().filter(a -> Integer.parseInt(a.getDate().substring(6)) >= finalDateFrom
                        && Integer.parseInt(a.getDate().substring(6)) <= dateTo)
                .toList();

        for (int j = 0; j < interestMonthInterestPortion.size(); j++) {
          int interestRateDay = Integer.parseInt(interestMonthInterestPortion.get(j).getDate().substring(6));
          double nextInterestRate = Double.parseDouble(interestMonthInterestPortion.get(j).getRate());

          if (interestRateDay >= dateFrom && interestRateDay <= dateTo) {
            annualizedInterest += eodBalancePrior * currentInterestRate / 100 * (interestRateDay - dateFrom);
            dateFrom = interestRateDay;
            currentInterestRate = nextInterestRate;
            System.out.println("HERE date from is: " + dateFrom + " and currentInterestRate is: " + currentInterestRate);
          }
        }
        annualizedInterest += eodBalancePrior * currentInterestRate / 100 * (dateTo - dateFrom);
        dateFrom = dateTo;
      }
    }

    YearMonth yearMonth = YearMonth
            .of(Integer.parseInt(interestCalcYearMonth.substring(0, 4)),
                    Integer.parseInt(interestCalcYearMonth.substring(4)));
    int monthLength = yearMonth.lengthOfMonth();
    Double monthLastBalance = Double.parseDouble(transactionHistory.lastElement().getBalance());

    int finalDateFrom2 = dateFrom;
    List<InterestRateDetail> interestMonthInterestPortion2 = interestMonthInterest
            .stream().filter(a -> Integer.parseInt(a.getDate().substring(6)) >= finalDateFrom2
                    && Integer.parseInt(a.getDate().substring(6)) <= monthLength)
            .toList();

    for (int k = 0; k < interestMonthInterestPortion2.size(); k++) {
      int interestRateDay = Integer.parseInt(interestMonthInterestPortion2.get(k).getDate().substring(6));
      double nextInterestRate = Double.parseDouble(interestMonthInterestPortion2.get(k).getRate());

      if (interestRateDay >= dateFrom && interestRateDay <= monthLength) {
        annualizedInterest += monthLastBalance * currentInterestRate / 100 * (interestRateDay - dateFrom);
        dateFrom = interestRateDay;
        currentInterestRate = nextInterestRate;
        System.out.println("HERE date from is: " + dateFrom + " and currentInterestRate is: " + currentInterestRate);
      }
    }
    annualizedInterest += monthLastBalance * currentInterestRate / 100 * (monthLength - dateFrom + 1);


    System.out.println(
            "Month " + interestCalcYearMonth.substring(4) + " interest rate is: " + annualizedInterest);

  }

  private static double calculateAnnualizedInterest(TransactionDetail transactionDetail,
                                                    List<InterestRateDetail> interestMonthInterest,
                                                    Double eodBalancePrior,
                                                    Integer dateFrom,
                                                    Double currentInterestRate) {
    double annualizedInterest = 0D;
    int dateTo = Integer.parseInt(transactionDetail.getDate().substring(6));

    int finalDateFrom = dateFrom;
    List<InterestRateDetail> interestMonthInterestPortion = interestMonthInterest
            .stream().filter(a -> Integer.parseInt(a.getDate().substring(6)) >= finalDateFrom
                    && Integer.parseInt(a.getDate().substring(6)) <= dateTo)
            .toList();
    System.out.println("HERE interestMonthInterestPortion is :" + interestMonthInterestPortion);

    for (int i = 0; i < interestMonthInterestPortion.size(); i++) {
      int interestRateDay = Integer.parseInt(interestMonthInterestPortion.get(i).getDate().substring(6));
      double nextInterestRate = Integer.parseInt(interestMonthInterestPortion.get(i).getRate());

      if (interestRateDay >= dateFrom && interestRateDay <= dateTo) {
        annualizedInterest += eodBalancePrior * currentInterestRate / 100 * (interestRateDay - dateFrom);
        dateFrom = interestRateDay;
        currentInterestRate = nextInterestRate;
        System.out.println("HERE date from is: " + dateFrom + " and currentInterestRate is: " + currentInterestRate);
      }
    }
    annualizedInterest += eodBalancePrior * currentInterestRate / 100 * (dateTo - dateFrom + 1);
    dateFrom = dateTo + 1;
    return annualizedInterest;
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
