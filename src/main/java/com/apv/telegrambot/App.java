package com.apv.telegrambot;

import com.mashape.unirest.http.exceptions.UnirestException;

public class App {
	public static void main(String[] args) throws UnirestException {
		System.out.println("Starting BoredomBusterBot");
		new BoredomBusterBot();
	}
}
