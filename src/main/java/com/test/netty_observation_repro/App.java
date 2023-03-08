package com.test.netty_observation_repro;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Hooks;
import reactor.netty.http.observability.ReactorNettyPropagatingSenderTracingObservationHandler;
import reactor.netty.observability.ReactorNettyTimerObservationHandler;
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

    // Does not create one and the same Timer every time
    @Bean
    @Order(500)
    ReactorNettyTimerObservationHandler timerObservationHandler (MeterRegistry meterRegistry) {
        return new ReactorNettyTimerObservationHandler(meterRegistry);
    }

    @Bean
    @Order(600)
    TracingAwareMeterObservationHandler<Observation.Context> myTracingAwareMeterObservationHandler(
            MeterRegistry meterRegistry, Tracer tracer) {
        DefaultMeterObservationHandler delegate = new DefaultMeterObservationHandler(meterRegistry);
        return new TracingAwareMeterObservationHandler<>(delegate, tracer);
    }

    // Specifies span tags for `http`
    @Bean
    @Order(700)
    ReactorNettyPropagatingSenderTracingObservationHandler propagatingSenderTracingObservationHandler(
            Tracer tracer, Propagator propagator) {
        return new ReactorNettyPropagatingSenderTracingObservationHandler(tracer, propagator);
    }

    // Specifies span tags for `connect`, `tls handshake`, `hostname resolution`
    @Order(800)
    @Bean
    ReactorNettyTracingObservationHandler tracingObservationHandler(Tracer tracer) {
        return new ReactorNettyTracingObservationHandler(tracer);
    }

    @Bean
    @Order(1000)
    public PropagatingReceiverTracingObservationHandler<?> myPropagatingReceiverTracingObservationHandler(
            Tracer tracer, Propagator propagator) {
        return new PropagatingReceiverTracingObservationHandler<>(tracer, propagator);
    }

    @Bean
    @Order(2000)
    public PropagatingSenderTracingObservationHandler<?> myPropagatingSenderTracingObservationHandler(
            Tracer tracer, Propagator propagator) {
        return new PropagatingSenderTracingObservationHandler<>(tracer, propagator);
    }

    @Bean
    @Order(2147482647)
    public DefaultTracingObservationHandler myDefaultTracingObservationHandler(Tracer tracer) {
        return new DefaultTracingObservationHandler(tracer);
    }
}