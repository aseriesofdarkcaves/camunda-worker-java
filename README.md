# camunda-worker-java

## Description

WIP

Attempts at setting up a Java-based worker for Camunda.

You'll need to add the file `application.properties` with all relevant info to
the `src/main/resources` folder (see the spring-zeebe Github page for details).

I make no guarantees that any of this stuff is correct, try at your own peril ;)

In fact, right now most of this is probably wrong!

## Links

- https://training.camunda.com/camunda8-dev/master/ - Exercises taken from here
- https://github.com/camunda-community-hub/spring-zeebe
- https://github.com/camunda/camunda-platform-get-started
- https://github.com/camunda-community-hub/camunda-8-process-solution-template
- https://github.com/berndruecker/customer-onboarding-camunda-8-springboot
- https://github.com/camunda-consulting/ca058-solutions/tree/exercise-7

## Sorting stuff out

### Probably the old solution

Trying to sort out the various solutions and make sense of what is going on...

```java
@Component
public class CreditDeductionWorker {
    // Comparing this to the other solution below, I am guessing that there was no auto-completion of jobs
    @ZeebeWorker(type = "credit-deduction")
    public void handleCreditDeduction(final JobClient jobClient, final ActivatedJob job) {
        // Note that here we have to manually fetch the variables we need
        Map<String, Object> variables = job.getVariablesAsMap();
        String customerId = variables.get("customerId").toString();
        Double orderTotal = Double.valueOf(variables.get("orderTotal").toString());

        CustomerService creditService = new CustomerService();
        double customerCredit = creditService.getCustomerCredit(customerId);
        double openAmount = creditService.deductCredit(customerCredit, orderTotal);

        // To communicate the new/updated variables to the process, we need to put them into the Map
        variables.put("customerCredit", customerCredit);
        variables.put("openAmount", openAmount);

        // Here we complete the job and push the variables Map to the process via the job client
        // I think newer implementations allow auto-completion implicitly, but don't quote me on that...
        jobClient.newCompleteCommand(job).variables(variables).send().join();
    }
}
```

### Probably the new solution

```java
@Component
public class CreditDeductionWorker {
    // TODO: find out why you would choose autoComplete = false here
    @JobWorker(type = "credit-deduction", autoComplete = false)
    // Note that we use the @Variable annotation to auto-map the process variables to the method parameters
    public void handleCreditDeduction(final JobClient jobClient,
                                      final ActivatedJob job,
                                      @Variable String customerId,
                                      @Variable double orderTotal) {
    CustomerService creditService = new CustomerService();
    double customerCredit = creditService.getCustomerCredit(customerId);
    double openAmount = creditService.deductCredit(customerCredit, orderTotal);

    // To communicate the new/updated variables to the process, we need to put them into the Map
    Map<String, Object> variables = new HashMap<>();
    variables.put("openAmount", openAmount);
    variables.put("customerCredit", customerCredit);
    
    /*
        TODO: I guess this is related to setting autoComplete = false in @JobWorker,
         but I am currently unsure of the reasoning behind wanting a manual job completion...
         My guess is that you have to manually send the Map with the variables?
     */
    jobClient.newCompleteCommand(job)
        .variables(variables)
        .send().exceptionally(throwable -> {
            throw new RuntimeException("Could not complete job " + job, throwable);
        });
    }
}
```
