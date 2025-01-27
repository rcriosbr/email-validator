package br.com.rcrios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import javax.mail.internet.AddressException;
import org.junit.jupiter.api.Test;

class EmailValidatorUtilsTests {

    @Test
    void validGetHostname() {
        String hostname = EmailValidatorUtils.getHostname("email@rcrios.com.BR");
        assertEquals("rcrios.com.br", hostname);
    }

    @Test
    void invalidGetHostname() {
        String hostname = EmailValidatorUtils.getHostname(null);
        assertEquals("", hostname);

        hostname = EmailValidatorUtils.getHostname(" ");
        assertEquals("", hostname);

        hostname = EmailValidatorUtils.getHostname("@");
        assertEquals("", hostname);

        hostname = EmailValidatorUtils.getHostname("bad email");
        assertEquals("", hostname);
    }

    @Test
    void isValidEmailAddress() {
        boolean valid = EmailValidatorUtils.isValidEmailAddress("XXXXXXXXXXXXXXXXXXX");
        assertEquals(false, valid);

        valid = EmailValidatorUtils.isValidEmailAddress(null);
        assertEquals(false, valid);

        valid = EmailValidatorUtils.isValidEmailAddress(" ");
        assertEquals(false, valid);

        valid = EmailValidatorUtils.isValidEmailAddress("@");
        assertEquals(false, valid);

        valid = EmailValidatorUtils.isValidEmailAddress("bad email");
        assertEquals(false, valid);

        valid = EmailValidatorUtils.isValidEmailAddress("valid@rcrios.com.br");
        assertEquals(true, valid);
    }

    @Test
    void validateEmailAddress() {
        Exception exception = assertThrows(AddressException.class, () -> {
            EmailValidatorUtils.validateEmailAddress("bad email");
        });

        String expectedMessage = "whitespace";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
