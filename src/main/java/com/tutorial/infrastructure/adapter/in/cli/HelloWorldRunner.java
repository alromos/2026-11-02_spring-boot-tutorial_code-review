package com.tutorial.infrastructure.adapter.in.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Driving adapter: runs once when the Spring context has started
 * and prints the greeting to the console.
 */
@Component
public class HelloWorldRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldRunner.class);

    @Override
    public void run(String... args) {
        log.info("hello world");
    }
}
