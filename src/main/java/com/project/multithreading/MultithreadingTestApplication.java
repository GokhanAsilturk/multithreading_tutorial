package com.project.multithreading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.project.multithreading")
public class MultithreadingTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultithreadingTestApplication.class, args);
    }

}
