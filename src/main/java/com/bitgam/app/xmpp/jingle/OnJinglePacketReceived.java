package com.bitgam.app.xmpp.jingle;

import com.bitgam.app.entities.Account;
import com.bitgam.app.xmpp.PacketReceived;
import com.bitgam.app.xmpp.jingle.stanzas.JinglePacket;

public interface OnJinglePacketReceived extends PacketReceived {
	void onJinglePacketReceived(Account account, JinglePacket packet);
}
