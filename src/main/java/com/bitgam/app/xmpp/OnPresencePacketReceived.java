package com.bitgam.app.xmpp;

import com.bitgam.app.entities.Account;
import com.bitgam.app.xmpp.stanzas.PresencePacket;

public interface OnPresencePacketReceived extends PacketReceived {
	void onPresencePacketReceived(Account account, PresencePacket packet);
}
