package com.david.processor;

import com.david.model.InterestRateDetail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

class InterestRateProcessorTest {

  private final static PrintStream originalOut = System.out;

  @AfterAll
  public static void restoreStream() {
    System.setOut(originalOut);
  }
  @Test
  @DisplayName("Input valid interest rule with empty interest history")
  void defineInterestRule() {
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputInterest = "20201001 RULE01 2";

    InputStream in = new ByteArrayInputStream(inputInterest.getBytes());
    System.setIn(in);

    InterestRateProcessor.defineInterestRule(interestRateHistory);
    assertEquals(interestRateHistory.get(0).getDate(), "20201001");
    assertEquals(interestRateHistory.get(0).getRuleId(), "RULE01");
    assertEquals(interestRateHistory.get(0).getRate(), "2");
  }

  @Test
  @DisplayName("Input valid interest rule on same day with existing interest history")
  void defineInterestRule2() {
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    InterestRateDetail interestRateDetail = new InterestRateDetail();
    interestRateDetail.setDate("20201001");
    interestRateDetail.setRuleId("RULE01");
    interestRateDetail.setRate("2");
    interestRateHistory.add(interestRateDetail);

    String inputInterest = "20201001 RULE02 3";

    InputStream in = new ByteArrayInputStream(inputInterest.getBytes());
    System.setIn(in);

    InterestRateProcessor.defineInterestRule(interestRateHistory);
    assertEquals(interestRateHistory.get(0).getDate(), "20201001");
    assertEquals(interestRateHistory.get(0).getRuleId(), "RULE02");
    assertEquals(interestRateHistory.get(0).getRate(), "3");
  }

  @Test
  @DisplayName("Input valid interest rule with existing interest history on previous days")
  void defineInterestRule3() {
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    InterestRateDetail interestRateDetail = new InterestRateDetail();
    interestRateDetail.setDate("20201001");
    interestRateDetail.setRuleId("RULE01");
    interestRateDetail.setRate("2");
    interestRateHistory.add(interestRateDetail);

    String inputInterest = "20201002 RULE02 3";

    InputStream in = new ByteArrayInputStream(inputInterest.getBytes());
    System.setIn(in);

    InterestRateProcessor.defineInterestRule(interestRateHistory);
    assertEquals(interestRateHistory.get(1).getDate(), "20201002");
    assertEquals(interestRateHistory.get(1).getRuleId(), "RULE02");
    assertEquals(interestRateHistory.get(1).getRate(), "3");
  }

  @Test
  @DisplayName("One of the fields is absent with empty interest rate history")
  void invalidInterestRuleInput1() {
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputTransaction = "20201001 2";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    InterestRateProcessor.defineInterestRule(interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid interest rate payload: " +
            "Invalid payload or one of the info is missing: <Date> <RuleId> <Rate in %%>");
  }

  @Test
  @DisplayName("Valid input with interest rate <=0 or >=100")
  void invalidInterestRuleInput2() {
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputTransaction = "20201001 RULE01 100";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    InterestRateProcessor.defineInterestRule(interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid interest rate payload: " +
            "Interest rate input should be between 0 - 100");
  }

  @Test
  @DisplayName("Interest rate input with invalid date format")
  void invalidInterestRuleInput3() {
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputTransaction = "2020-10-01 RULE01 100";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    InterestRateProcessor.defineInterestRule(interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid interest rate payload found: " +
            "Invalid date format input. Text '2020-10-01' could not be parsed at index 4");
  }

  @Test
  @DisplayName("Interest rate input with date earlier than previous interest rate rule date")
  void invalidInterestRuleInput4() {
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    InterestRateDetail interestRateDetail = new InterestRateDetail();
    interestRateDetail.setDate("20201001");
    interestRateDetail.setRuleId("RULE01");
    interestRateDetail.setRate("2");
    interestRateHistory.add(interestRateDetail);

    String inputTransaction = "20200930 RULE02 1.5";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    InterestRateProcessor.defineInterestRule(interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid interest rate payload: " +
            "Interest rate input date is earlier than previous rate date.");
  }
}