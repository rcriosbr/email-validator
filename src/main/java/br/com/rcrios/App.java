package br.com.rcrios;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			throw new IllegalArgumentException("Usage: java -jar email-validator.jar /Users/somebady/email_list.txt");
		}

		App app = new App();
		app.read(args[0]);
	}

	private void read(String pathToFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				write(pathToFile, line, validateEmail(line));
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void write(String pathToFile, String line, String result) {
		String output = line.concat(";").concat(result).concat("\n");

		try {
			Files.write(Paths.get(pathToFile.concat(System.currentTimeMillis() + ".txt")), output.getBytes(),
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private String validateEmail(String emailToBeValidated) {
		String result = "OK";

		try {
			EmailValidatorUtils.validate(emailToBeValidated);
		} catch (EmailValidatorException e) {
			result = e.getMessage();
		}

		return result;
	}
}
