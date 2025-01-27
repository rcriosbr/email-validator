package br.com.rcrios;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.mail.internet.AddressException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to help DNS manipulation
 */
public class DNSUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(DNSUtils.class);

	/**
	 * JNDI DNS Service Provider supported resource record type
	 */
	public static final String[] RECORD_TYPES = new String[] { "A", "NS", "CNAME", "SOA", "PTR", "MX", "TXT", "HINFO", "AAAA",
			"NAPTR", "SRV" };

	private static String MX_RECORD_TYPE = "MX";

	private static Map<String, SortedMap<Integer, String>> cache = new HashMap<>();

	/**
	 * Perform a DNS lookup for MX records into the provided email address.
	 * 
	 * @see http://docs.oracle.com/javase/7/docs/technotes/guides/jndi/jndi-dns.
	 *      html
	 * 
	 * @param emailAddress
	 *            Email address from which the hostname will extracted in order
	 *            to retrieve the MX records.
	 */
	public SortedMap<Integer, String> getMX(String email) {
		this.validateArgument(email);

		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

		String hostname = EmailValidatorUtils.getHostname(email);

		if (!cache.containsKey(hostname)) {
			LOGGER.debug("Trying to get MX records from {}", hostname);

			Attribute attribute = null;
			try {
				InitialDirContext idc = new InitialDirContext(env);
				Attributes attributes = idc.getAttributes(hostname, new String[] { MX_RECORD_TYPE });
				attribute = attributes.get(MX_RECORD_TYPE);
			} catch (NamingException e) {
				LOGGER.error("Error getting MX records from " + hostname, e);
			}

			SortedMap<Integer, String> servers = parseMXRecord(attribute);

			LOGGER.debug("Found servers: {}", servers);

			cache.put(hostname, servers);
		}
		return cache.get(hostname);
	}

	/**
	 * Validates if the supplied argument is valid. Verifies against null/empty
	 * and if it is well formed.
	 * 
	 * @param email
	 *            Argument to be validated. If not, thorws an
	 *            IllegalArgumentException.
	 */
	private void validateArgument(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("The provided 'email' argument cannot be null or empty.");
		}

		try {
			EmailValidatorUtils.validateEmailAddress(email);
		} catch (AddressException e) {
			throw new IllegalArgumentException("The provided 'email' is invalid: " + e.getMessage());
		}
	}

	/**
	 * DNS Service Provider returns resource records as an Attibute, that will
	 * result into a String such as "MX: 5 gmail-smtp-in.l.google.com., 40
	 * alt4.gmail-smtp-in.l.google.com., 10 alt1.gmail-smtp-in.l.google.com.".
	 * 
	 * @param attribute
	 *            Object to be parsed
	 * 
	 * @return A SortedMap. The keys are the servers priority and the value, the
	 *         server hostname.
	 */
	private SortedMap<Integer, String> parseMXRecord(Attribute attribute) {
		SortedMap<Integer, String> result = new TreeMap<>();

		LOGGER.debug("Parsing {}", attribute);

		if (attribute != null) {
			// Remove "MX: "
			String tmp = attribute.toString().split(":")[1].trim();

			// Does the domain has more than 1 server?
			if (tmp.contains(",")) {
				// Split servers into an array
				String[] servers = tmp.split(",");
				for (String server : servers) {
					this.process(result, server);
				}
			} else {
				this.process(result, tmp);
			}
		}

		return result;
	}

	/**
	 * Split "10 alt1.gmail-smtp-in.l.google.com." and store it into a map. It
	 * also removes the last dot from the provided line.
	 * 
	 * @param result
	 *            The map where the processed server will be stored
	 * 
	 * @param line
	 *            String that will be split.
	 */
	private void process(SortedMap<Integer, String> result, String line) {
		String[] tokens = line.trim().split(" ");
		result.put(Integer.valueOf(tokens[0]), tokens[1].substring(0, tokens[1].length() - 1));

	}

	public void checkMX(String email) {
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

		String hostname = email.substring(email.indexOf("@") + 1, email.length()).toLowerCase().trim();

		try {
			InitialDirContext idc = new InitialDirContext(env);
			Attributes attributes = idc.getAttributes(hostname, new String[] { MX_RECORD_TYPE });
			Attribute attribute = attributes.get(MX_RECORD_TYPE);
			
			LOGGER.debug("{}", attribute);
			
		} catch (NamingException e) {
			LOGGER.error("Error checking MX records from '{}'", email, e);
		}
	}
}
