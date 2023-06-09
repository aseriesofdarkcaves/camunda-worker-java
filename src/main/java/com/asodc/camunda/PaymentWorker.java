package com.asodc.camunda;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker class to handle tasks related to the payment process.
 */
@Component
public class PaymentWorker {
    /**
     * Logger for the PaymentWorker.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentWorker.class);

    /**
     * Gets the credit of the customer with the id {@code customerId}.
     * As this is an example implementation, this method takes the last two numbers
     * of the customer ID and returns it as the customer credit value.
     *
     * @param customerId the ID of the customer for which to get the credit
     */
    @JobWorker(type = "getCustomerCredit", autoComplete = false)
    public void getCustomerCredit(final JobClient jobClient,
                                  final ActivatedJob job,
                                  @Variable String customerId,
                                  @Variable double orderTotal) {
        LOGGER.info("Started worker for job type : " + job.getType());

        // generate some fake credit
        double customerCredit = Double.parseDouble(customerId.substring(customerId.length() - 2));
        LOGGER.info("Credit for customerId " + customerId + ": " + customerCredit);

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerCredit", customerCredit);

        LOGGER.info("VARIABLES MAP: " + variables);

        // complete the job and merge the variables map back to the process
        jobClient.newCompleteCommand(job)
                .variables(variables)
                .send()
                .join();

        LOGGER.info("Worker finished!");
    }

    /**
     * The exercise calls this method "deductCredit", but I don't like that name.
     * I also don't like the suggested second parameter name "amountToDeduct".
     * Returns the open amount for an order, after applying customer credit, if any.
     * If the customer credit is greater than the amount, the end payment amount is 0.0,
     * otherwise the end payment amount is the original payment amount minus the customer's credit.
     *
     * @param customerCredit the customer's current credit
     * @param amountToPay    the initial amount of the payment, without credit deductions
     */
    @JobWorker(type = "getEndPaymentAmount", autoComplete = false)
    public void getEndPaymentAmount(final JobClient jobClient,
                                    final ActivatedJob job,
                                    @Variable Double customerCredit,
                                    @Variable Double amountToPay) {
        LOGGER.info("Started worker for job type : " + job.getType());

        // calculate the end amount to be paid by the customer
        double endAmountToPay = customerCredit > amountToPay ? 0.0 : amountToPay - customerCredit;
        LOGGER.info("End amount to pay: " + endAmountToPay);

        Map<String, Object> variables = new HashMap<>();
        variables.put("orderTotal", endAmountToPay);

        // complete the job and merge the variables map back to the process
        jobClient.newCompleteCommand(job)
                .variables(variables)
                .send()
                .join();

        LOGGER.info("Worker finished!");
    }
}
