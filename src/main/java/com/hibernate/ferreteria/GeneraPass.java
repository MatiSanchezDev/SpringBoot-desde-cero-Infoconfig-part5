package com.hibernate.ferreteria;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneraPass {

    static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("admin123"));
    }

}
