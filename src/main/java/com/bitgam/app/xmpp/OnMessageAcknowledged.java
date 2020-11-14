package com.bitgam.app.xmpp;

import com.bitgam.app.entities.Account;

public interface OnMessageAcknowledged {
	boolean onMessageAcknowledged(Account account, String id);
}
