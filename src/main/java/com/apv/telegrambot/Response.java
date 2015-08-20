package com.apv.telegrambot;

public class Response {

	int chatId;

	boolean disableWebPagePreview;

	String text;

	public int getChatId() {
		return chatId;
	}

	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

	public boolean isDisableWebPagePreview() {
		return disableWebPagePreview;
	}

	public void setDisableWebPagePreview(boolean disableWebPagePreview) {
		this.disableWebPagePreview = disableWebPagePreview;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
