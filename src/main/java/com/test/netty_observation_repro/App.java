package com.test.netty_observation_repro;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import static reactor.netty.Metrics.OBSERVATION_REGISTRY;

import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.propagation.Propagator;
import reactor.core.publisher.Hooks;
import reactor.netty.http.observability.ReactorNettyPropagatingSenderTracingObservationHandler;
import reactor.netty.observability.ReactorNettyTracingObservationHandler;

@SpringBootApplication
public class App {

    public static void main(String[] args) {

        Hooks.enableAutomaticContextPropagation();

        new SpringApplicationBuilder( App.class )
                .web( WebApplicationType.REACTIVE )
                .run( args )
                .registerShutdownHook();

    }

    @Bean
    public ObservationRegistry observationRegistry(Tracer tracer, Propagator propagator) {
        OBSERVATION_REGISTRY.observationConfig()
                .observationHandler(
                        new ObservationHandler.FirstMatchingCompositeObservationHandler(
                                new ReactorNettyPropagatingSenderTracingObservationHandler(tracer, propagator),
                                new ReactorNettyTracingObservationHandler(tracer),
                                new DefaultTracingObservationHandler(tracer)
                )
        );
        return OBSERVATION_REGISTRY;
    }

}