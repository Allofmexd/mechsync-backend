package com.mechsync.modules.workorders.domain.exception;
public class InvalidWorkOrderDatesException extends RuntimeException {
    public InvalidWorkOrderDatesException() { super("Estimated delivery date cannot be before estimated start date"); }
}
