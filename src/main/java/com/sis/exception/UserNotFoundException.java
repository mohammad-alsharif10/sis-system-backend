package com.sis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class UserNotFoundException extends RuntimeException{
	private static final long serialVersionUID = 2426256104172639927L;
	public UserNotFoundException(long userId)
	{
		super("User  : " + userId + " doesn't exists");
	}

}
