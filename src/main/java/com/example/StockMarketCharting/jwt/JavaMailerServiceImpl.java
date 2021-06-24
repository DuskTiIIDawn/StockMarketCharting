package com.example.StockMarketCharting.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service("mailService")
public class JavaMailerServiceImpl {

	@Autowired
	JavaMailSenderImpl EmailSender;

	private MailSender mailSender;

	public JavaMailerServiceImpl() {
		this.mailSender = EmailSender;
	}

	public void sendMail(String to, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		mailSender.send(message);
	}
}