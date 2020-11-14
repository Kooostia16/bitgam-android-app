package com.bitgam.app.xmpp;

import com.bitgam.app.entities.Account;
import com.bitgam.app.xmpp.stanzas.MessagePacket;

public interface OnMessagePacketReceived extends PacketReceived {
	void onMessagePacketReceived(Account account, MessagePacket packet);
}
