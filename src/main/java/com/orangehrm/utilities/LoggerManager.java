package com.orangehrm.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LoggerManager {

    private LoggerManager() {
        // Prevent object creation
    }

    public static Logger getLogger(Class<?> clazz) {
        if(clazz == null){
            throw new IllegalArgumentException("Logger class can not be null");
        }
        return LogManager.getLogger(clazz);
    }
}