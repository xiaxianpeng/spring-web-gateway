package com.example.springwebgateway.service;


import org.springframework.stereotype.Service;

@Service
public class VerifyServiceImpl implements VerifyService {


    @Override
    public boolean verifyIp(String ip) {
        return false;
    }
}
