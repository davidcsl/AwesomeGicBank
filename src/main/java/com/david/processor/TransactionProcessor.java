package com.david.processor;

import com.david.model.InterestRateDetail;
import com.david.model.InterestRateDto;
import com.david.model.TransactionDetail;

import java.io.Console;
import java.time.LocalDate;
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

    if (transactionRequest.isBlank()) {
      return;
    }
    String[] payloads = transactionRequest.split(" ");

    try {
      transactionPayloadValidation(payloads);
      String transactionDate = payloads[0];
      String accountId = payloads[1];
      String type = payloads[2];
      Double amount = Double.parseDouble(payloads[3]);

      if (transactionHistory.size() > 0 && !"I".equalsIgnoreCase(transactionHistory.lastElement().getType())) {
        Integer monthDifference =
                Integer.parseInt(transactionDate.substring(0, 6))
                        - Integer.parseInt(transactionHistory.lastElement().getDate().substring(0, 6));
        if (monthDifference > 0) {
          interestPayout(transactionHistory, monthDifference, interestRateHistory);
        }
      }

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
      transactionHistory.add(transactionDetail);

      System.out.println("\nAccount: " + accountId);
      System.out.printf("| %-10s | %-12s | %-10s | %10s | %n", "Date", "Txn Id", "Type", "Amount");
      transactionHistory.forEach(
              a -> System.out.printf("| %-10s | %-12s | %-10s | %10s | %n",
                      a.getDate(), a.getTransactionId(), a.getType(), a.getAmount()));

      System.out.println("\nIs there anything else you'd like to do?");
    } catch (DateTimeParseException e) {
      System.out.println("Invalid transaction payload found: Invalid date format input. " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Invalid transaction payload: " + e.getMessage());
    }

  }

  private static void interestPayout(Vector<TransactionDetail> transactionHistory,
                                     Integer monthDifference,
                                     Vector<InterestRateDetail> interestRateHistory) {

    TransactionDetail lastTransaction = transactionHistory.lastElement();
    String interestCalcYearMonth = lastTransaction.getDate().substring(0, 6);

    List<TransactionDetail> interestMonthTransaction = transactionHistory
            .stream()
            .filter(a -> interestCalcYearMonth.equalsIgnoreCase(a.getDate().substring(0, 6)))
            .toList();
    List<InterestRateDetail> interestMonthInterest = interestRateHistory
            .stream()
            .filter(a -> interestCalcYearMonth.equalsIgnoreCase(a.getDate().substring(0, 6)))
            .toList();

    InterestRateDto interestRateDto = new InterestRateDto();
    interestRateDto.setCurrentInterestRate(0D);

    if (interestRateHistory.size() > 0)  {
      String extractedInitialRate = interestRateHistory
              .stream()
              .filter(a -> Integer.parseInt(a.getDate().substring(0, 6)) < Integer.parseInt(interestCalcYearMonth))
              .sorted((s1, s2) -> Integer.compare(Integer.parseInt(s2.getDate()), Integer.parseInt(s1.getDate())))
              .map(InterestRateDetail::getRate)
              .findFirst()
              .orElse(null);
      if (Objects.nonNull(extractedInitialRate)) {
        interestRateDto.setCurrentInterestRate(Double.parseDouble(extractedInitialRate));
      }
    }

    interestRateDto.setAnnualizedInterest(0D);
    interestRateDto.setDateFrom(1);
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
        calculateInterestPayout(interestMonthInterest, interestRateDto, dateTo, eodBalancePrior);
      }
    }

    YearMonth yearMonth = YearMonth
            .of(Integer.parseInt(interestCalcYearMonth.substring(0, 4)),
                    Integer.parseInt(interestCalcYearMonth.substring(4)));
    int lastDayOfTheMonth = yearMonth.lengthOfMonth();
    Double monthLastBalance = Double.parseDouble(lastTransaction.getBalance());
    calculateInterestPayout(interestMonthInterest, interestRateDto, lastDayOfTheMonth, monthLastBalance);

    double calculatedAnnualizedInterest = Math.round(interestRateDto.getAnnualizedInterest() / 365 * 100.0) / 100.0;
    double newBalance = Double.parseDouble(lastTransaction.getBalance()) + calculatedAnnualizedInterest;

    TransactionDetail interestTransactionDetail = new TransactionDetail();
    interestTransactionDetail.setAccountId(lastTransaction.getAccountId());
    interestTransactionDetail.setDate(lastTransaction.getDate().substring(0, 6) + lastDayOfTheMonth);
    interestTransactionDetail.setAmount(String.valueOf(calculatedAnnualizedInterest));
    interestTransactionDetail.setType("I");
    interestTransactionDetail.setBalance(String.valueOf(newBalance));
    interestTransactionDetail.setTransactionId("");
    transactionHistory.add(interestTransactionDetail);

  }

  private static void calculateInterestPayout(List<InterestRateDetail> interestMonthInterest,
                                              InterestRateDto interestRateDto,
                                              Integer dateTo,
                                              Double eodBalancePrior) {

    int finalDateFrom = interestRateDto.getDateFrom();
    List<InterestRateDetail> interestMonthInterestPortion = interestMonthInterest
            .stream().filter(a -> Integer.parseInt(a.getDate().substring(6)) >= finalDateFrom
                    && Integer.parseInt(a.getDate().substring(6)) <= dateTo)
            .toList();

    for (int j = 0; j < interestMonthInterestPortion.size(); j++) {
      int interestRateDay = Integer.parseInt(interestMonthInterestPortion.get(j).getDate().substring(6));
      double nextInterestRate = Double.parseDouble(interestMonthInterestPortion.get(j).getRate());

      if (interestRateDay >= interestRateDto.getDateFrom() && interestRateDay <= dateTo) {
        double newAnnualizedInterest = eodBalancePrior * interestRateDto.getCurrentInterestRate()
                / 100 * (interestRateDay - interestRateDto.getDateFrom());
        interestRateDto.setAnnualizedInterest(interestRateDto.getAnnualizedInterest() + newAnnualizedInterest);

        interestRateDto.setDateFrom(interestRateDay);
        interestRateDto.setCurrentInterestRate(nextInterestRate);
      }
    }

    double newAnnualizedInterest = eodBalancePrior * interestRateDto.getCurrentInterestRate()
            / 100 * (dateTo - interestRateDto.getDateFrom());
    interestRateDto.setAnnualizedInterest(interestRateDto.getAnnualizedInterest() + newAnnualizedInterest);
    interestRateDto.setDateFrom(dateTo);
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
