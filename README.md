# camunda-worker-java

Attempts at setting up a Java-based worker for Camunda.

You'll need to add the file `application.properties` with all relevant info to
the `src/main/resources` folder (see the spring-zeebe Github page for details).

I make no guarantees that any of this stuff is correct, try at your own peril ;)

Exercises taken from here:

https://training.camunda.com/camunda8-dev/master/

This is the Spring Zeebe library I used

https://github.com/camunda-community-hub/spring-zeebe

This stuff is deprecated though according to the logs when starting the app...

```
You are using the deprecated 'spring-zeebe-starter' dependency. Please update your POM:
  io.camunda : spring-zeebe-starter --> io.camunda.spring : spring-boot-starter-camunda
```

But if you look up spring-boot-starter-camunda: https://github.com/camunda/camunda-bpm-spring-boot-starter

The README.md has this message...

```
This repository moved here: https://github.com/camunda/camunda-bpm-platform/tree/master/spring-boot-starter
```

Which if you follow https://github.com/camunda/camunda-bpm-platform/tree/master/spring-boot-starter seems to be part of
Camunda 7...

So I really don't know what's going on.
