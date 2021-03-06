package com.neu.strangers.adapter;

/**
 * Created with Android Studio.
 * Author: Enex Tapper
 * Date: 15/5/31
 * Project: Strangers
 * Package: com.neu.strangers.adapter
 */
public class ContactAdapterItem {
	private String userName;
	private String pinyin;
	private int id;

	public ContactAdapterItem(int id,String userName, String pinyin) {
		this.id = id;
		this.userName = userName;
		this.pinyin = pinyin;
	}

	public String getUserName() {
		return userName;
	}

	public String getPinyin() {
		return pinyin;
	}

	public int getId(){
		return id;
	}
}
