# Reactor-Netty Observation issue repro

Reproducible example for the tracing issue discussed in [#2791](https://github.com/reactor/reactor-netty/issues/2719)

The observability stack for receiving logs/traces/metrics is in [stack/](/stack/), taken from https://github.com/marcingrzejszczak/observability-boot-blog-post with some small edits  
Make sure that it is up before running  
Note that Prometheus uses `host.docker.internal`, if on Linux use `--add-host=host.docker.internal:host-gateway`  
(or edit [src/main/resources/*](/src/main/resources/) to use existing servers if desired)

Compile and run using `mvn compile exec:java` (note: Java 17+)

Then hit http://localhost:8085/test with a GET request  
One of the log entries in the terminal should have the trace ID to query on Tempo (http://localhost:3000/explore)
