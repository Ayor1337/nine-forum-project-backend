package com.ayor;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class exampleTest {

    @Resource
    PasswordEncoder encoder;

    //$2a$10$/m4hMr/yL5BuHF6axCRUq.e90VT26j.ANItT4J.45S9cFRbgMyCXC
    @Test
    public void test() {
        System.out.println(encoder.encode("123"));
    }

}
