package com.david.processor;

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


class PrintStatementProcessorTest {

  private final static PrintStream originalOut = System.out;

  @AfterAll
  public static void restoreStream() {
    System.setOut(originalOut);
  }

  @Test
  @DisplayName("Print Statement Input with valid payload with empty transaction history")
  void inputPrintStatement() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();

    String inputPrintStatement = "ABC 202010";

    InputStream in = new ByteArrayInputStream(inputPrintStatement.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    PrintStatementProcessor.printStatement(transactionHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"There is no transactions");
  }

  @Test
  @DisplayName("Print Statement Input with valid payload with existing transaction history")
  void inputPrintStatement2() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();
    TransactionDetail transactionDetail = new TransactionDetail();
    transactionDetail.setAccountId("ABC");
    transactionDetail.setType("D");
    transactionDetail.setDate("20201009");
    transactionDetail.setTransactionId("20201009-01");
    transactionDetail.setAmount("30.0");
    transactionDetail.setBalance("30.0");
    transactionHistory.add(transactionDetail);

    String inputPrintStatement = "ABC 202010";

    InputStream in = new ByteArrayInputStream(inputPrintStatement.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    PrintStatementProcessor.printStatement(transactionHistory);
    String[] outputRawArray = outContent.toString().split("\n");
    String outputRaw = outputRawArray[outputRawArray.length-1];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Is there anything else you'd like to do?");
  }

  @Test
  @DisplayName("Print Statement Input with payload with some fields is absent")
  void invalidInputPrintStatement1() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();

    String inputPrintStatement = "202010";

    InputStream in = new ByteArrayInputStream(inputPrintStatement.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    PrintStatementProcessor.printStatement(transactionHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid transaction payload: " +
            "Invalid payload or one of the info is missing: <Account> <Year><Month>");
  }

  @Test
  @DisplayName("Print Statement Input with payload with invalid date format")
  void invalidInputPrintStatement2() {
    Vector<TransactionDetail> transactionHistory = new Vector<>();

    String inputPrintStatement = "ABC 2020/10";

    InputStream in = new ByteArrayInputStream(inputPrintStatement.getBytes());
    System.setIn(in);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    PrintStatementProcessor.printStatement(transactionHistory);
    String outputRaw = outContent.toString().split("\n")[2];
    String output = outputRaw.substring(0, outputRaw.length()-1);

    assertEquals(output,"Invalid Print Statement payload found: " +
            "Invalid date format input. Text '2020/10' could not be parsed at index 4");
  }
}