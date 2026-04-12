package com.riloidx.todolist.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String field, String value) {
        super("Task with " + field + "=" + value + " not found");
    }
}
