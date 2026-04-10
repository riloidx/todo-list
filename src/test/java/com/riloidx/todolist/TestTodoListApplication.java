package com.riloidx.todolist;

import org.springframework.boot.SpringApplication;

public class TestTodoListApplication {

    public static void main(String[] args) {
        SpringApplication.from(TodoListApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
