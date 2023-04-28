# camunda-worker-java

## Description

This is a work in progress, there are likely many issues.

Attempts at setting up a Java-based worker for `Camunda Platform 8`.

## Setup

This project uses `JDK 17`.

All of this stuff uses a Camunda Cloud Cluster, so you'll need to set that up first (out of scope).

When you have that set up, you'll also need to create the Payment Process in BPMN (it's included in the resouces).

To connect the application to the cluster, you'll need to add the file `application.properties` with all relevant info
to the `src/main/resources` folder (replace the values with those in your Camunda console).
See the [spring-zeebe Github](https://github.com/camunda-community-hub/spring-zeebe) page for more details.

```properties
zeebe.client.cloud.region=region
zeebe.client.cloud.clusterId=clusterId
zeebe.client.cloud.clientId=clientId
zeebe.client.cloud.clientSecret=clientSecret
```

## Execution

Start the main method to launch the application.

Use `zbctl` to send a message and trigger the process.

Note you will have to either set environment variables for the various connection parameters, or in-line them with flags
in the command, like so:

```
zbctl create instance PaymentProcess --variables "{\"customerId\": \"12345\", \"orderTotal\": 45.99}" \
 --address address.zeebe.camunda.io:443 \
 --clientId clientId \
 --clientSecret clientSecret \
 --authzUrl https://login.cloud.camunda.io/oauth/token
```

Check the console for worker log messages and `Camunda Operate` to check on the status of the process.

## Use of Maps in Workers

To get existing variables from the process you need to have them declared in your method parameters with the `@Variable`
annotation.

Additionally, only the variables which have been annotated will be included in the Map returned
by `job.getVariablesAsMap()`.

New and existing variables can be added/merged with the existing ones in the process via

```java
Map<String, Object> variables = new HashMap<>();
variables.put("customerCredit", customerCredit);
```

## Links

- https://training.camunda.com/camunda8-dev/master/ - Exercises taken from here
- https://github.com/camunda-consulting/ca058-solutions/tree/exercise-7 - Solutions to the exercises (main branch seems
  to be a bit outdated)
- https://github.com/camunda-community-hub/spring-zeebe
- https://github.com/camunda/camunda-platform-get-started
- https://github.com/camunda-community-hub/camunda-8-process-solution-template
- https://github.com/berndruecker/customer-onboarding-camunda-8-springboot

## Worker Implementations

The [example repo](https://github.com/camunda-consulting/ca058-solutions) which I have been using to figure this stuff
out shows multiple ways of implementing workers in Java.

Note that both examples use "Services" to encapsulate business logic away from the worker class.

The job can be controlled in the code via the job client - I think the new version completes the job automatically by
default, which is why the annotation specifies `autoComplete = false`.

### The "old" way

```java
...

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

### The "new" way (probably)

```java
...

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
