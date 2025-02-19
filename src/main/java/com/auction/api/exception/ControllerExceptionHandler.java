package com.auction.api.exception;

import com.auction.api.model.ErrorResponse;
import com.auction.exception.AuctionClosedException;
import com.auction.exception.AuctionCreationException;
import com.auction.exception.AuctionModificationException;
import com.auction.exception.AuctionTimeExpiredException;
import com.auction.exception.AuthenticationException;
import com.auction.exception.BidCreationException;
import com.auction.exception.InvalidBidException;
import com.auction.exception.InvalidCredentialsException;
import com.auction.exception.InvalidRoleException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.exception.UserConflictException;
import com.auction.exception.UserCreationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
		log.error("Resource not found exception {}", exception.getMessage());
		return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ErrorResponse> handleValidationErrorException(AuctionModificationException e) {
		log.error("AuctionModificationException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ErrorResponse> handleValidationErrorException(AuctionClosedException e) {
		log.error("AuctionClosedException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ErrorResponse> handleValidationErrorException(AuctionTimeExpiredException e) {
		log.error("AuctionTimeExpiredException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ErrorResponse> handleValidationErrorException(InvalidBidException e) {
		log.error("InvalidBidException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ErrorResponse> handleValidationErrorException(IllegalArgumentException e) {
		log.error("ValidationErrorException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
		BindingResult result = ex.getBindingResult();
		StringBuilder message = new StringBuilder();

		for (FieldError fieldError : result.getFieldErrors()) {
			message.append(fieldError.getField())
					.append(": ")
					.append(fieldError.getDefaultMessage())
					.append("\n");
		}

		return new ResponseEntity<>(message.toString(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UserConflictException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ResponseEntity<ErrorResponse> handleUserConflictException(UserConflictException e) {
		log.warn("UserConflictException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
		log.error("AccessDeniedException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(PropertyValueException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ErrorResponse> handlePropertyValueException(PropertyValueException exception) {
		log.error("Property value exception: {}", exception.getMessage(), exception);
		return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
		log.error("Unexpected exception occurred: {}", exception.getMessage(), exception);
		return new ResponseEntity<>(new ErrorResponse(
				"An unexpected error occurred. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException e) {
		log.error("InvalidCredentialsException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(InvalidRoleException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ErrorResponse> handleInvalidRoleException(InvalidRoleException e) {
		log.error("InvalidRoleException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
		log.error("AuthenticationException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UserCreationException.class)
	public ResponseEntity<ErrorResponse> handleAuctionOptimisticLockException(UserCreationException e) {
		log.warn("UserCreationException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(BidCreationException.class)
	public ResponseEntity<ErrorResponse> handleAuctionOptimisticLockException(BidCreationException e) {
		log.warn("BidCreationException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(AuctionCreationException.class)
	public ResponseEntity<ErrorResponse> handleAuctionOptimisticLockException(AuctionCreationException e) {
		log.warn("AuctionCreationException received, message: {}", e.getMessage());
		return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
