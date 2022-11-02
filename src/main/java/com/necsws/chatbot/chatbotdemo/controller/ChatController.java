package com.necsws.chatbot.chatbotdemo.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.necsws.chatbot.chatbotdemo.model.ChatMessage;
import com.necsws.chatbot.chatbotdemo.service.ChatBotService;

@RestController
public class ChatController {

	@Autowired
	ChatBotService chatbotService;
	
	@PostMapping("/predict")
	public ChatMessage chatMessage(@RequestBody ChatMessage message) {
		ChatMessage response = new ChatMessage();
		try {
			response.setAnswer(chatbotService.getMessage(message.getMessage()));
		} catch (IOException e) {
			System.out.println(e.getMessage());
			response.setAnswer("Sorry something went wrong. Try after sometime!!!");
		}
		return response;
	}
}
