package br.com.rcrios;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidatorUtils {
	private static final Logger logger = LoggerFactory.getLogger(EmailValidatorUtils.class);

	private static DNSUtils dns = new DNSUtils();
	private static final int MINIMUM_SIZE = 5;

	/**
	 * Hides utility class constructor
	 */
	private EmailValidatorUtils() {
		// Does nothing
	}


	/**
	 * Extracts the hostname from an email address.
	 * 
	 * @param email The email to be processed.
	 * 
	 * @return The hostname. If the email is not valid (e.g.: null or without "@"), will return an empty
	 *         string.
	 */
	public static String getHostname(String email) {
		if (email == null || email.trim().isEmpty() || email.indexOf("@") < 0) {
			return "";
		}

		return email.substring(email.indexOf("@") + 1, email.length()).toLowerCase().trim();
	}

	/**
	 * Convenience method to validate an email.
	 * 
	 * @param emailToBeValidated Email to be validated
	 * 
	 * @throws EmailValidatorException The exception message contains the detail of way it was thrown.
	 */
	public static void validate(String emailToBeValidated) throws EmailValidatorException {
		if (emailToBeValidated == null || emailToBeValidated.trim().isEmpty()) {
			throw new EmailValidatorException("Null or empty");
		} else {
			if (emailToBeValidated.length() < MINIMUM_SIZE || containsIvalidString(emailToBeValidated)) {
				throw new EmailValidatorException("Invalid");
			} else if (!EmailValidatorUtils.isValidEmailAddress(emailToBeValidated)) {
				throw new EmailValidatorException("Not well formed");
			} else if (EmailValidatorUtils.isFake(emailToBeValidated)) {
				throw new EmailValidatorException("Fake");
			} else {
				SortedMap<Integer, String> servers = dns.getMX(emailToBeValidated);
				if (servers.isEmpty()) {
					throw new EmailValidatorException("No MX");
				}
			}
		}
	}

	/**
	 * Convenience method that shut up the exception (it is logged as INFO).
	 * 
	 * @see EmailValidatorUtils#validateEmailAddress(String)
	 * 
	 * @param email Email to be validated
	 * 
	 * @return True if it's valid. False otherwise.
	 */
	public static boolean isValidEmailAddress(String email) {
		boolean result = true;
		try {
			validateEmailAddress(email);
		} catch (AddressException ex) {
			logger.info("The email '{}' is not valid: {}", email, ex.getMessage());
			result = false;
		}
		return result;
	}

	/**
	 * Validates an email
	 * 
	 * @see javax.mail.internet.InternetAddress#validate()
	 * 
	 * @param email Email to be validates
	 * 
	 * @throws AddressException If its not valid
	 */
	public static void validateEmailAddress(String email) throws AddressException {
		if (email == null) {
			throw new AddressException("Null email");
		}
		InternetAddress emailAddr = new InternetAddress(email);
		emailAddr.validate();
	}

	public static boolean containsIvalidString(String email) {
		boolean result = false;
		Set<String> set = invalidSet();
		for (String str : set) {
			if (email.contains(str)) {
				result = true;
				break;
			}
		}

		return result;
	}

	public static boolean isFake(String email) {
		boolean result = false;

		Set<String> set = fakeSet();
		for (String str : set) {
			if (email.contains(str)) {
				result = true;
				break;
			}
		}

		return result;
	}

	private static Set<String> invalidSet() {
		Set<String> set = new HashSet<>();
		set.add("@@");
		set.add("*");
		set.add(",");
		set.add(".com.com");
		set.add(".br.br");
		set.add("null");

		return set;
	}

	private static Set<String> fakeSet() {
		Set<String> set = new HashSet<>();
		set.add("teste");
		set.add("naolembra");
		set.add("nao.com");
		set.add("nao@nao");
		set.add("clienteoutbound");
		set.add("aliancamotoshonda");
		set.add("naotem");
		set.add("ntem");
		set.add("proposta");
		set.add("possui");

		return set;
	}
}
