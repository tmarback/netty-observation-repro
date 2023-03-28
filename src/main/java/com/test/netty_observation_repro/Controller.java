package com.test.netty_observation_repro;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.ObservationRegistry;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import static reactor.netty.Metrics.OBSERVATION_REGISTRY;

@RestController
@RequestMapping("/test")
public class Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger( Controller.class );

    private final ObservationRegistry registry;
    private final HttpClient http;

    public Controller(ObservationRegistry registry) {
        this.registry = registry;
        OBSERVATION_REGISTRY = registry;
        this.http = HttpClient.create().metrics( true, Function.identity() );
    }

    private Mono<String> getPage(String host) {

        return http.baseUrl( host )
                .get()
                .uri( "/" )
                .response()
                .map( HttpClientResponse::status )
                .map( HttpResponseStatus::code )
                .map( String::valueOf )
                .name( "app.request.one" )
                .tag( "host", host )
                .tap( Micrometer.observation( registry ) );

    }

    @GetMapping
    public Mono<String> getTest() {

        return Flux.just( "https://www.google.com", "https://www.facebook.com", "https://www.github.com" )
                .doOnSubscribe( s -> LOGGER.info( "Beginning" ) )
                .flatMap( this::getPage )
                .collectList()
                .doOnNext( r -> LOGGER.info( "Results: " + r ) )
                .map( r -> String.join( " ", r ) )
                .doOnSuccess( r -> LOGGER.info( "Ending" ) )
                .name( "app.request" )
                .tap( Micrometer.observation( registry ) );

    }
    
}
