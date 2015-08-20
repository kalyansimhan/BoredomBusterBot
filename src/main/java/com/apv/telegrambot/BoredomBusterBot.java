package com.apv.telegrambot;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class BoredomBusterBot {

	private static final String BASE_URL = "https://api.telegram.org/bot113090639:AAFrlslpl0fuJbqqbST4X9NeTJlS5EHaSTo/";
	private static final String POLLING_URL = BASE_URL + "getUpdates";
	private static final String SEND_MESSAGE_URL = BASE_URL + "sendMessage";

	private static final String XKCD_BASE_URL = "http://xkcd.com/info.0.json";
	private static final String IMGUR_BASE_URL = "https://api.imgur.com/3/topics/2/top/day/";
	private static final String IMGUR_AUTH = "CLIENT-ID 77ce2c0bea5966a";

	BoredomBusterBot() throws UnirestException {
		poll(0);
	}

	private void poll(int offset) throws UnirestException {

		HttpResponse<JsonNode> asJson = Unirest.get(POLLING_URL)
				.queryString("timeout", 60).queryString("offset", offset)
				.asJson();
		JSONObject jsonObject = new JSONObject(asJson.getBody().getObject()
				.toString());
		if (asJson.getStatus() == 200 && jsonObject.getBoolean("ok")) {
			int next = getUpdateId(jsonObject);
			handleRequest(jsonObject);
			poll(next);
		}
	}

	// ------------------- Generic message handlers -------------------

	private int getUpdateId(JSONObject jsonObject) {
		JSONArray jsonArray = jsonObject.getJSONArray("result");
		if (jsonArray.length() > 0) {
			JSONObject obj = (JSONObject) jsonArray.get(jsonArray.length() - 1);
			return obj.getInt("update_id") + 1;
		}
		return 0;
	}

	private void handleRequest(JSONObject jsonObject) throws UnirestException {
		System.out.println("Handling incoming request");
		JSONArray results = jsonObject.getJSONArray("result");
		for (int i = 0; i < results.length(); i++) {
			JSONObject object = (JSONObject) results.get(i);
			JSONObject message = object.getJSONObject("message");
			JSONObject chat = message.getJSONObject("chat");
			int chatId = chat.getInt("id");

			if ("/bored".equals(message.opt("text"))
					|| "@BoredomBusterBot".equals(message.opt("text"))
					|| message.opt("new_chat_participant") != null) {
				if (object.getInt("update_id") % 2 == 0) {
					fetchFromXKCD(chatId);
				} else {
					fetchFromImgur(chatId);
				}
			} else if ("/start".equals(message.opt("text"))) {
				sendWelcomeMessage(chatId);
			} else {
				sendFailureMessage(chatId);
			}
		}
	}

	private int getRandomNumber(int min, int max) {
		return (int) (Math.floor(Math.random() * (max - min + 1)) + min);
	}

	// ------------------- IMGUR -------------------------------------

	private void fetchFromImgur(int chatId) throws UnirestException {
		System.out.println("Fetching from imgur");
		int pageNo = getRandomNumber(1, 20);
		Comic comic = getImgurComic(pageNo);
		Response response = buildSuccessMessage(chatId, comic);
		sendSuccessMessage(response);
	}

	private Comic getImgurComic(int pageNo) throws UnirestException {
		Comic comic = null;
		HttpResponse<JsonNode> asJson = Unirest.get(IMGUR_BASE_URL + pageNo)
				.header("Authorization", IMGUR_AUTH).asJson();
		if (asJson.getStatus() == 200) {
			JSONObject jsonObject = new JSONObject(asJson.getBody().getObject()
					.toString());
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			int comicNo = getRandomNumber(1, jsonArray.length() - 1);

			JSONObject comicObject = (JSONObject) jsonArray.get(comicNo);
			comic = new Comic(comicObject, Comic.Type.IMGUR);
		}
		return comic;
	}

	// ------------------- XKCD -------------------------------------

	private void fetchFromXKCD(int chatId) throws UnirestException {
		System.out.println("Fetching from xkcd");
		int max = getMaxXKCD();
		int comicNumber = getRandomNumber(1, max);
		Comic comic = getXKCDComic(comicNumber);
		Response response = buildSuccessMessage(chatId, comic);
		sendSuccessMessage(response);
	}

	private Comic getXKCDComic(int comicNumber) throws UnirestException {
		Comic comic = null;
		String comicUrl = "http://xkcd.com/" + comicNumber + "/info.0.json";
		HttpResponse<JsonNode> asJson = Unirest.get(comicUrl).asJson();
		if (asJson.getStatus() == 200) {
			JSONObject jsonObject = new JSONObject(asJson.getBody().getObject()
					.toString());
			comic = new Comic(jsonObject, Comic.Type.XKCD);
		}
		return comic;
	}

	private int getMaxXKCD() throws UnirestException {
		HttpResponse<JsonNode> asJson = Unirest.get(XKCD_BASE_URL).asJson();
		if (asJson.getStatus() == 200) {
			JSONObject jsonObject = new JSONObject(asJson.getBody().getObject()
					.toString());
			return jsonObject.getInt("num");
		}
		return 0;
	}

	// -------------------- Sending message ---------------------------------

	private void sendFailureMessage(int chatId) throws UnirestException {
		HttpResponse<JsonNode> asJson = Unirest
				.post(SEND_MESSAGE_URL)
				.field("chat_id", chatId)
				.field("text",
						"I know you are bored but unless you tell me in a language I understand you will continue to be bored. Send /bored to me and I promise to kill you boredom.")
				.asJson();
		if (asJson.getStatus() == 200) {
			System.out.println("successfully sent failure message for "
					+ chatId);
		}
	}

	private Response buildSuccessMessage(int chatId, Comic comic) {
		Response response = new Response();
		response.setChatId(chatId);
		response.setDisableWebPagePreview(false);
		response.setText(comic.getTitle() + " " + comic.getUrl());
		return response;
	}

	private void sendSuccessMessage(Response response) throws UnirestException {
		HttpResponse<JsonNode> asJson = Unirest
				.post(SEND_MESSAGE_URL)
				.field("chat_id", response.getChatId())
				.field("disable_web_page_preview",
						response.isDisableWebPagePreview())
				.field("text", response.getText()).asJson();
		if (asJson.getStatus() == 200) {
			System.out.println("successfully sent comic for "
					+ response.getChatId());
		}
	}

	private void sendWelcomeMessage(int chatId) throws UnirestException {
		HttpResponse<JsonNode> asJson = Unirest
				.post(SEND_MESSAGE_URL)
				.field("chat_id", chatId)
				.field("text",
						"Hello Pal! Looks like you are bored. Send /bored to me and kill you boredom.")
				.asJson();
		if (asJson.getStatus() == 200) {
			System.out.println("successfully sent welcome message for "
					+ chatId);
		}
	}

}
