package com.david.processor;

import com.david.model.InterestRateDetail;
import com.david.model.TransactionDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeneralProcessorTest {

  @Mock
  private Vector<TransactionDetail> transactionHistory;

  @Mock
  private Vector<InterestRateDetail> interestRateHistory;

  @Test
  @DisplayName("Test display action method")
  void displayActions() {
    String input = "T";

    InputStream in = new ByteArrayInputStream(input.getBytes());
    System.setIn(in);

    assertEquals(GeneralProcessor.displayActions(), "T");
  }

  @Test
  @DisplayName("Input valid action")
  void processActionWithTransactionProcessor() {
    String inputAction = "T";

    InputStream in = new ByteArrayInputStream("20201010 ABC D 30".getBytes());
    System.setIn(in);

    assertTrue(GeneralProcessor.processAction(inputAction, transactionHistory, interestRateHistory));
  }

  @Test
  @DisplayName("Input quit action")
  void processActionWithQuit() {
    String inputAction = "Q";

    assertFalse(GeneralProcessor.processAction(inputAction, transactionHistory, interestRateHistory));
  }

  @Test
  @DisplayName("Input invalid action")
  void processActionWithInvalidInput() {
    String inputAction = "F";

    assertTrue(GeneralProcessor.processAction(inputAction, transactionHistory, interestRateHistory));
  }
}