package com.asodc.camunda;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Worker class to handle tasks related to the payment process.
 */
@Component
public class PaymentWorker {
    /**
     * Zeebe client.
     */
    @Autowired
    private ZeebeClient client;

    // TODO: I think I may have to return Maps in these methods...

    /**
     * Gets the credit of the customer with the id {@code customerId}.
     * As this is an example implementation, this method takes the last two numbers
     * of the customer ID and returns it as the customer credit value.
     *
     * @param customerId the ID of the customer for which to get the credit
     * @return the customer's current credit
     */
    @JobWorker(type = "guthaben-auslesen")
    public Double getCustomerCredit(String customerId) {
        String fakeCredit = customerId.substring(customerId.length() - 2);

        return Double.parseDouble(fakeCredit);
    }

    /**
     * The exercise calls this method "deductCredit", but I don't like that name.
     * I also don't like the suggested second parameter name "amountToDeduct".
     * Returns the open amount for an order.
     * If the customer credit is greater than the amount, the end payment amount is 0.0,
     * otherwise the end payment amount is the original payment amount minus the customer's credit.
     *
     * @param customerCredit the customer's current credit
     * @param amountToPay    the initial amount of the payment, without credit deductions
     * @return the end payment amount after the customer's credit has been applied
     */
    @JobWorker(type = "guthaben-belasten")
    public Double getEndPaymentAmount(Double customerCredit, Double amountToPay) {
        if (customerCredit > amountToPay)
            return 0.0;
        else {
            return amountToPay - customerCredit;
        }
    }
}
