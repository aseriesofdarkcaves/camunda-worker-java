package com.asodc.camunda;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExampleWorkerTests {
    private ExampleApplication worker;

    private static final String EXPECTED_ZERO_AMOUNT_MSG = "The customer credit should cover all of the end amount to pay";

    @BeforeEach
    public void createNewWorkerInstance() {
        worker = new ExampleApplication();
    }

    /**
     * Test to ensure that the {@code getCustomerCredit} implementation uses the last two
     * digits of the customerId to generate the customer credit value.
     */
    @Test
    public void checkGetCustomerCreditLogic() {
        final String customerId = "12345";

        final Double customerCredit = worker.getCustomerCredit(customerId);

        Assertions.assertEquals(45.0, customerCredit, "The method didn't return the expected value");
    }

    /**
     * When the customer credit is greater than the amount to pay,
     * the end amount should be 0.0.
     */
    @Test
    public void endPaymentCreditGreater() {
        final Double customerCredit = 100.0;
        final Double amountToDeduct = 50.0;

        final Double endAmount = worker.getEndPaymentAmount(customerCredit, amountToDeduct);

        Assertions.assertEquals(0.0, endAmount, EXPECTED_ZERO_AMOUNT_MSG);
    }

    /**
     * When the customer credit is equal to the amount to pay,
     * the end amount should be 0.0.
     */
    @Test
    public void endPaymentCreditEqual() {
        final Double customerCredit = 100.0;
        final Double amountToPay = 100.0;

        final Double endAmount = worker.getEndPaymentAmount(customerCredit, amountToPay);

        Assertions.assertEquals(0.0, endAmount, EXPECTED_ZERO_AMOUNT_MSG);
    }

    /**
     * When the customer credit partially covers the amount to pay,
     * the end amount should be the initial amount to pay, minus the customer credit.
     */
    @Test
    public void endPaymentPartialCreditCoverage() {
        final Double customerCredit = 10.0;
        final Double amountToPay = 50.0;
        final Double expectedEndAmount = amountToPay - customerCredit;

        final Double endAmount = worker.getEndPaymentAmount(customerCredit, amountToPay);

        Assertions.assertEquals(expectedEndAmount, endAmount, "The end amount was not as expected");
    }
}
