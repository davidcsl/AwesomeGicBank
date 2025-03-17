package com.david.processor;

import com.david.model.InterestRateDetail;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Scanner;
import java.util.Vector;

public class InterestRateProcessor {

  public static void defineInterestRule(Vector<InterestRateDetail> interestRateHistory) {

    Scanner scanner = new Scanner(System.in);
    System.out.println("Please enter interest rules details in <Date> <RuleId> <Rate in %%> format \n" +
            "(or enter blank to go back to main menu):");
    String interestRateRequest = scanner.nextLine();

    if (interestRateRequest.isBlank()) {
      return;
    }

    String[] payloads = interestRateRequest.split(" ");

    try {
      interestRatePayloadValidation(payloads);
      String newInterestDate = payloads[0];
      String ruleId = payloads[1];
      String interestRate = payloads[2];

      InterestRateDetail lastInterestRateDetails;
      InterestRateDetail newInterestRateDetails = new InterestRateDetail();
      newInterestRateDetails.setDate(newInterestDate);
      newInterestRateDetails.setRuleId(ruleId);
      newInterestRateDetails.setRate(interestRate);

      if (interestRateHistory.size() > 0) {
        lastInterestRateDetails = interestRateHistory.lastElement();
        if (Integer.parseInt(newInterestDate) < Integer.parseInt(lastInterestRateDetails.getDate())) {
          throw new Exception("Interest rate input date is earlier than previous rate date.");
        }
        if (Integer.parseInt(newInterestDate) == Integer.parseInt(lastInterestRateDetails.getDate())) {
          interestRateHistory.set(interestRateHistory.size() - 1, newInterestRateDetails);
        } else {
          interestRateHistory.add(newInterestRateDetails);
        }

      } else {
        interestRateHistory.add(newInterestRateDetails);
      }

      System.out.println("\nInterest rules:");
      System.out.printf("| %-10s | %-10s | %10s |%n", "Date", "RuleId", "Rate (%)");
      interestRateHistory.forEach(
              a -> System.out.printf("| %-10s | %-10s | %10s |%n", a.getDate(), a.getRuleId(), a.getRate()));
      System.out.println("\nIs there anything else you'd like to do?");

    } catch (DateTimeParseException e){
      System.out.println("Invalid interest rate payload found: Invalid date format input. " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Invalid interest rate payload: " + e.getMessage());
    }

  }

  private static void interestRatePayloadValidation(String[] payloads) throws Exception {
    if (payloads.length != 3) {
      throw new Exception("Invalid payload or one of the info is missing: <Date> <RuleId> <Rate in %%>");
    }

    LocalDate date = LocalDate.parse(payloads[0], DateTimeFormatter.BASIC_ISO_DATE);
    String ruleId = payloads[1];
    Double interestRate = Double.parseDouble(payloads[2]);

    if (Objects.isNull(ruleId)) {
      throw new Exception("Rule id is null.");
    } else if (interestRate <= 0 || interestRate >= 100) {
      throw new Exception("Interest rate input should be between 0 - 100");
    }
  }
}
