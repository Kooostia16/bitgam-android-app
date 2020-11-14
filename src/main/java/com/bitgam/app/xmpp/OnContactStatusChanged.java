package com.bitgam.app.xmpp;

import com.bitgam.app.entities.Contact;

public interface OnContactStatusChanged {
	void onContactStatusChanged(final Contact contact, final boolean online);
}
