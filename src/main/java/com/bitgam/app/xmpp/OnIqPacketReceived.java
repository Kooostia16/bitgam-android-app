package com.bitgam.app.xmpp;

import com.bitgam.app.entities.Account;
import com.bitgam.app.xmpp.stanzas.IqPacket;

public interface OnIqPacketReceived extends PacketReceived {
	void onIqPacketReceived(Account account, IqPacket packet);
}
