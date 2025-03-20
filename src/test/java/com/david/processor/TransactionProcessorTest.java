package com.david.processor;

import com.david.model.InterestRateDetail;
import com.david.model.TransactionDetail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

class TransactionProcessorTest {

  private final static PrintStream originalOut = System.out;

  @AfterAll
  public static void restoreStream() {
    System.setOut(originalOut);
  }

  @Test
  @DisplayName("Input new deposit with empty transaction history and empty interest")
  void inputTransactionDeposit1() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputTransaction = "20201009 ABC D 30";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    assertEquals(transactionHistory.get(0).getAccountId(), "ABC");
    assertEquals(transactionHistory.get(0).getType(), "D");
    assertEquals(transactionHistory.get(0).getDate(), "20201009");
    assertEquals(transactionHistory.get(0).getTransactionId(), "20201009-01");
    assertEquals(transactionHistory.get(0).getAmount(), "30.0");
    assertEquals(transactionHistory.get(0).getBalance(), "30.0");
  }

  @Test
  @DisplayName("Input new deposit with existing same day transaction history and empty interest")
  void inputTransactionDeposit2() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201009");
    transactionDetail.setTransactionId("20201009-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    String inputTransaction = "20201009 ABC D 30";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    assertEquals(transactionHistory.get(1).getAccountId(), "ABC");
    assertEquals(transactionHistory.get(1).getType(), "D");
    assertEquals(transactionHistory.get(1).getDate(), "20201009");
    assertEquals(transactionHistory.get(1).getTransactionId(), "20201009-02");
    assertEquals(transactionHistory.get(1).getAmount(), "30.0");
    assertEquals(transactionHistory.get(1).getBalance(), "60.0");
  }

  @Test
  @DisplayName("Input new next month deposit with existing different day transaction history and empty interest")
  void inputTransactionDeposit3() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201009");
    transactionDetail.setTransactionId("20201009-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    String inputTransaction = "20201109 ABC D 30";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    assertEquals(transactionHistory.get(2).getAccountId(), "ABC");
    assertEquals(transactionHistory.get(2).getType(), "D");
    assertEquals(transactionHistory.get(2).getDate(), "20201109");
    assertEquals(transactionHistory.get(2).getTransactionId(), "20201109-01");
    assertEquals(transactionHistory.get(2).getAmount(), "30.0");
    assertEquals(transactionHistory.get(2).getBalance(), "60.0");
  }

  @Test
  @DisplayName("Input new deposit in new month " +
          "with existing previous month transaction history and existing interest")
  void inputTransactionDeposit4() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201009");
    transactionDetail.setTransactionId("20201009-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    InterestRateDetail interestRateDetail = new InterestRateDetail();
    interestRateDetail.setDate("20201001");
    interestRateDetail.setRuleId("RULE01");
    interestRateDetail.setRate("2");
    interestRateHistory.add(interestRateDetail);

    String inputTransaction = "20201109 ABC D 30";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    assertEquals(transactionHistory.get(2).getAccountId(), "ABC");
    assertEquals(transactionHistory.get(2).getType(), "D");
    assertEquals(transactionHistory.get(2).getDate(), "20201109");
    assertEquals(transactionHistory.get(2).getTransactionId(), "20201109-01");
    assertEquals(transactionHistory.get(2).getAmount(), "30.0");
    assertEquals(transactionHistory.get(2).getBalance(), "60.04");
  }

  @Test
  @DisplayName("Input new withdrawal with sufficient balance and empty interest")
  void inputTransactionWithdrawal1() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201008");
    transactionDetail.setTransactionId("20201008-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    String inputTransaction = "20201009 ABC W 20";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    assertEquals(transactionHistory.get(1).getAccountId(), "ABC");
    assertEquals(transactionHistory.get(1).getType(), "W");
    assertEquals(transactionHistory.get(1).getDate(), "20201009");
    assertEquals(transactionHistory.get(1).getTransactionId(), "20201009-01");
    assertEquals(transactionHistory.get(1).getAmount(), "20.0");
    assertEquals(transactionHistory.get(1).getBalance(), "10.0");
  }

  @Test
  @DisplayName("Input new withdrawal with insufficient balance and empty interest")
  void inputTransactionWithdrawal2() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201008");
    transactionDetail.setTransactionId("20201008-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    String inputTransaction = "20201009 ABC W 40";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid transaction payload: Insufficient balance for withdrawal.");
  }

  @Test
  @DisplayName("Input new same day withdrawal with existing transaction and empty interest")
  void inputTransactionWithdrawal3() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201008");
    transactionDetail.setTransactionId("20201008-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    String inputTransaction = "20201008 ABC W 20";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    assertEquals(transactionHistory.get(1).getAccountId(), "ABC");
    assertEquals(transactionHistory.get(1).getType(), "W");
    assertEquals(transactionHistory.get(1).getDate(), "20201008");
    assertEquals(transactionHistory.get(1).getTransactionId(), "20201008-02");
    assertEquals(transactionHistory.get(1).getAmount(), "20.0");
    assertEquals(transactionHistory.get(1).getBalance(), "10.0");
  }

  @Test
  @DisplayName("Input new next month withdrawal with existing transaction and existing interest")
  void inputTransactionWithdrawal4() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201008");
    transactionDetail.setTransactionId("20201008-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    InterestRateDetail interestRateDetail = new InterestRateDetail();
    interestRateDetail.setDate("20201001");
    interestRateDetail.setRuleId("RULE01");
    interestRateDetail.setRate("2");
    interestRateHistory.add(interestRateDetail);

    String inputTransaction = "20201108 ABC W 20";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    assertEquals(transactionHistory.get(2).getAccountId(), "ABC");
    assertEquals(transactionHistory.get(2).getType(), "W");
    assertEquals(transactionHistory.get(2).getDate(), "20201108");
    assertEquals(transactionHistory.get(2).getTransactionId(), "20201108-01");
    assertEquals(transactionHistory.get(2).getAmount(), "20.0");
    assertEquals(transactionHistory.get(2).getBalance(), "10.04");
  }

  @Test
  @DisplayName("Input new Transaction without accountId with empty transaction history")
  void inputError1() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputTransaction = "20201009 D 40";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid transaction payload: Invalid payload or one of the info is missing: <Date> <Account> <Type> <Amount>");
  }

  @Test
  @DisplayName("Input new Transaction without type with empty transaction history")
  void inputError2() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputTransaction = "20201009 ABC 40";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid transaction payload: Invalid payload or one of the info is missing: <Date> <Account> <Type> <Amount>");
  }

  @Test
  @DisplayName("Input new Transaction with invalid date with empty transaction history")
  void inputError3() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputTransaction = "2020-10-09 ABC D 40";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid transaction payload found: Invalid date format input. Text '2020-10-09' could not be parsed at index 4");
  }

  @Test
  @DisplayName("Input new Transaction with invalid type with empty transaction history")
  void inputError4() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputTransaction = "20201009 ABC K 40";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid transaction payload: Invalid transaction type.");
  }

  @Test
  @DisplayName("Input new Transaction with negative amount with empty transaction history")
  void inputError5() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();

    String inputTransaction = "20201009 ABC D -40";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid transaction payload: Negative transaction amount detected.");
  }

  @Test
  @DisplayName("Input new Transaction with existing transaction history but different accountId")
  void inputError6() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201009");
    transactionDetail.setTransactionId("20201009-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    String inputTransaction = "20201009 XYZ D 30";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid transaction payload: Invalid transaction input account id.");
  }

  @Test
  @DisplayName("Input new Transaction of earlier date than existing transaction history")
  void inputError7() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    Vector<InterestRateDetail> interestRateHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201009");
    transactionDetail.setTransactionId("20201009-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    String inputTransaction = "20201007 ABC D 30";

    InputStream in = new ByteArrayInputStream(inputTransaction.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    TransactionProcessor.inputTransaction(transactionHistory, interestRateHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid transaction payload: Transaction input date is earlier than previous transaction.");
  }
}