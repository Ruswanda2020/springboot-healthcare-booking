package com.oneDev.healthcarebooking.exception;

import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException{

    private final ExceptionType type;
    public ApplicationException(ExceptionType type) {
        super(type.getMessage());
        this.type = type;
    }

    public ApplicationException(ExceptionType type, String customMessage) {
        super(customMessage);
        this.type = type;
    }
}
