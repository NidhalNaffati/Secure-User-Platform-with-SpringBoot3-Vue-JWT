package com.nidhal.backend.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

/**
 * This class is a controller advice that handles exceptions globally for all REST endpoints.
 * <p>
 * It uses various @ExceptionHandler methods to handle specific types of exceptions.
 * <p>
 * It also defines a private method buildResponseEntity() to build the response entity for each exception.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler {

    /**
     * Handles MethodArgumentNotValidException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @param exception the MethodArgumentNotValidException to be handled
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        List<String> errorList = exception
          .getBindingResult()
          .getFieldErrors()
          .stream()
          .map(DefaultMessageSourceResolvable::getDefaultMessage)
          .toList();

        ErrorResponse response = new ErrorResponse();
        response.setStatus(UNPROCESSABLE_ENTITY);
        response.setMessage(errorList.toString());
        return buildResponseEntity(response);
    }


    /**
     * Handles TypeMismatchException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @param exception the TypeMismatchException to be handled
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(TypeMismatchException.class)
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(NOT_IMPLEMENTED);
        response.setMessage(exception.getMessage());
        return buildResponseEntity(response);
    }


    /**
     * Handles PasswordDontMatchException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @param exception the PasswordDontMatchException to be handled
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(PasswordDontMatchException.class)
    public ResponseEntity<Object> handlePasswordDontMatchException(PasswordDontMatchException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(BAD_REQUEST);
        response.setMessage(exception.getReason());
        return buildResponseEntity(response);
    }


    /**
     * Handles UserNotFoundException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @param exception the UserNotFoundException to be handled
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(BAD_REQUEST);
        response.setMessage(exception.getReason());
        return buildResponseEntity(response);
    }


    /**
     * Handles EmailAlreadyExistsException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @param exception the EmailAlreadyExistsException to be handled
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(CONFLICT);
        response.setMessage(exception.getReason());
        return buildResponseEntity(response);
    }


    /**
     * Handles AccessDeniedException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDeniedException() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(FORBIDDEN);
        response.setMessage("You are not authorized to access this resource");
        return buildResponseEntity(response);
    }


    /**
     * Handles ExpiredJwtException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(REQUEST_TIMEOUT);
        response.setMessage("Your session has expired, please login again");
        return buildResponseEntity(response);
    }


    /**
     * Handles DisabledException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleDisabledException() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(FORBIDDEN);
        response.setMessage("Please ensure that you have clicked on the link that was sent to your email.");
        return buildResponseEntity(response);
    }


    /**
     * Handles BadCredentialsException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(BAD_REQUEST);
        response.setMessage("Invalid credentials");
        return buildResponseEntity(response);
    }



    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Object> handleAccountLockedException(AccountLockedException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(LOCKED);
        response.setMessage(exception.getReason());
        return buildResponseEntity(response);
    }


    /**
     * Handles HttpMessageNotReadableException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(BAD_REQUEST);
        response.setMessage("Malformed JSON request");
        return buildResponseEntity(response);
    }


    /**
     * Handles MailSendException and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @param exception the MailSendException to be handled
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<Object> handleUnknownHostException(MailSendException exception) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(INTERNAL_SERVER_ERROR);
        response.setMessage("An internal server error has occurred when trying to send the mail. " + exception.getFailedMessages());
        return buildResponseEntity(response);
    }


    /**
     * Handles any other type of Exception and returns a ResponseEntity with an ErrorResponse containing the error message.
     *
     * @param exception the Exception to be handled
     * @return a ResponseEntity with an ErrorResponse containing the error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAnyException(Exception exception) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(INTERNAL_SERVER_ERROR);
        error.setMessage("An internal server error has occurred." + exception.getMessage());
        return buildResponseEntity(error);
    }


    private ResponseEntity<Object> buildResponseEntity(ErrorResponse response) {
        return new ResponseEntity<>(response, response.getStatus());
    }

}
