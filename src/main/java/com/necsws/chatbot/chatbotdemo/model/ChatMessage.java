package com.necsws.chatbot.chatbotdemo.model;

public class ChatMessage {
	private String message;
	private String answer;
	
	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ChatMessage(String message) {
		super();
		this.message = message;
	}

	public ChatMessage() {
		super();
	}
}
