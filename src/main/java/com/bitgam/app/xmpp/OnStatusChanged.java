package com.bitgam.app.xmpp;

import com.bitgam.app.entities.Account;

public interface OnStatusChanged {
	void onStatusChanged(Account account);
}
