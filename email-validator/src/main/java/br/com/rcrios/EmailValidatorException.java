package br.com.rcrios;

public class EmailValidatorException extends Exception {

	private static final long serialVersionUID = 3852539341241943897L;

	public EmailValidatorException(String message) {
		super(message);
	}

	public EmailValidatorException(String message, Throwable cause) {
		super(message, cause);
	}
}
