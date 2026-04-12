package com.riloidx.todolist.exception;

public class TaskAlreadyExistsException extends RuntimeException {
    public TaskAlreadyExistsException(String field, String value) {
        super("Task with " + field + "=" + value + " already exists");
    }
}
