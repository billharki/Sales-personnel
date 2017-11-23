package com.example.sales.service;

public class Constants {
	public abstract class RequestParameters
    {
        public final static String  EMAIL = "EMAIL";
        public final static String  PASSWORD = "PASSWORD";
        public final static String  TEMPORARY_PASSWORD = "TEMPORARY_PASSWORD";
    }

    public abstract class ResponseMessages
    {
        public final static String NOT_LOGGED_IN = "NOT_LOGGED_IN";

        public final static String LOGGED_IN = "LOGGED_IN";

        public final static String INVALID_REQUEST = "INVALID_REQUEST";

        public final static String NO_SUCH_USER = "NO_SUCH_USER";

        public final static String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";

        public final static String USER_CREATED = "USER_CREATED";
        
        public final static String FORCE_PASSWORD_CHANGE = "FORCE_PASSWORD_CHANGE";
        
        public final static String INVALID_PASSWORD = "INVALID_PASSWORD";
    }

    public abstract class CookieNames
    {
        public final static String  ACCESS_TOKEN = "ACCESS_TOKEN";
        public final static String  REFRESH_TOKEN = "REFRESH_TOKEN";
    }
}
