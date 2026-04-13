package com.payment.payment.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "milestone-service")
public interface MilestoneClient {

    @PostMapping("/milestone/api/{id}/mark-paid")
    void markPaid(@PathVariable("id") Long id);
}
