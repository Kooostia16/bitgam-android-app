package com.bitgam.app.services;

import com.bitgam.app.BuildConfig;

public abstract class AbstractQuickConversationsService {

    protected final XmppConnectionService service;

    public AbstractQuickConversationsService(XmppConnectionService service) {
        this.service = service;
    }

    public abstract void considerSync();

    public static boolean isQuicksy() {
        return "quicksy".equals(BuildConfig.FLAVOR_mode);
    }

    public static boolean isConversations() {
        return "bitgam".equals(BuildConfig.FLAVOR_mode);
    }

    public abstract void signalAccountStateChange();

    public abstract boolean isSynchronizing();

    public abstract void considerSyncBackground(boolean force);
}
