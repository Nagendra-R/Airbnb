package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.entites.Booking;

public interface CheckoutService {

    String getSessionUrl(Booking booking, String successUrl, String failureUrl);

}
