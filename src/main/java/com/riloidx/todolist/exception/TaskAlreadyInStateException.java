package com.riloidx.todolist.exception;

public class TaskAlreadyInStateException extends RuntimeException {
    public TaskAlreadyInStateException(String message) {
        super(message);
    }
}
