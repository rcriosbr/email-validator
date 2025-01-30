package br.com.rcrios;

import org.junit.jupiter.api.Test;

class DNSUtilsTests {

    @Test
    void checkMX() {
        DNSUtils dns = new DNSUtils();
        dns.checkMX("email@cetelem.es");
    }
}
