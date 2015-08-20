package com.apv.telegrambot;

import org.json.JSONObject;

public class Comic {

	String title;
	String url;

	public Comic(JSONObject jsonObject, Type type) {
		switch (type) {
		case IMGUR:
			this.title = jsonObject.getString("title");
			this.url = "http://imgur.com/topic/Funny/"
					+ jsonObject.getString("id");
			break;
		case XKCD:
			this.title = jsonObject.getString("title");
			this.url = jsonObject.getString("img");
			break;
		}
	}

	enum Type {
		XKCD, IMGUR
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}
}
