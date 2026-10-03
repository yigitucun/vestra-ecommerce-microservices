package com.vestra.common.web.exception;

import org.springframework.http.HttpStatus;


public class ApiException extends RuntimeException {
    private final String title;
    private final String detail;
    private final HttpStatus httpStatus;


    public ApiException( String title, String detail,HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.title = title;
        this.detail = detail;
    }

    public static ApiException badRequest(String title,String detail){
        return new ApiException(title,detail,HttpStatus.BAD_REQUEST);
    }

    public static ApiException notFound(String title,String detail){
        return new ApiException(title,detail,HttpStatus.NOT_FOUND);
    }

    public static ApiException tooManyRequest(String title,String detail){
        return new ApiException(title,detail,HttpStatus.TOO_MANY_REQUESTS);
    }

    public static ApiException conflict(String title,String detail){
        return new ApiException(title,detail,HttpStatus.CONFLICT);
    }
    public static ApiException unAuthorized(String title,String detail){
        return new ApiException(title,detail,HttpStatus.UNAUTHORIZED);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }
}
