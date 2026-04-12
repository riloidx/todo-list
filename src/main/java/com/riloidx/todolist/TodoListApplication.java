package com.riloidx.todolist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class TodoListApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoListApplication.class, args);
    }

}
