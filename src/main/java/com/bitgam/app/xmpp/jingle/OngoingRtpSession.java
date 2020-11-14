package com.bitgam.app.xmpp.jingle;

import com.bitgam.app.entities.Account;
import com.bitgam.app.xmpp.Jid;

public interface OngoingRtpSession {
    Account getAccount();
    Jid getWith();
    String getSessionId();
}
