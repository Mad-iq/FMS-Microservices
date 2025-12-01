package com.notification.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.notification.model.EmailPayload;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingEmailListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = "booking.email.queue")
    public void consume(EmailPayload payload) {

        System.out.println("Received booking event: " + payload);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(payload.getEmail());
        msg.setSubject("Booking Confirmation - " + payload.getPnr());
        msg.setText("Hello " + payload.getName()
                + ",\nYour booking is confirmed.\nPNR: "
                + payload.getPnr()
                + "\nFlight ID: " + payload.getFlightId());

        mailSender.send(msg);

        System.out.println("Email sent to: " + payload.getEmail());
    }
}
