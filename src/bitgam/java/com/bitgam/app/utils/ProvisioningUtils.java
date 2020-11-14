package com.bitgam.app.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import java.util.List;

import com.bitgam.app.R;
import com.bitgam.app.entities.AccountConfiguration;
import com.bitgam.app.persistance.DatabaseBackend;
import com.bitgam.app.services.XmppConnectionService;
import com.bitgam.app.ui.EditAccountActivity;
import com.bitgam.app.xmpp.Jid;

public class ProvisioningUtils {

    public static void provision(final Activity activity, final String json) {
        final AccountConfiguration accountConfiguration;
        try {
            accountConfiguration = AccountConfiguration.parse(json);
        } catch (final IllegalArgumentException e) {
            Toast.makeText(activity, R.string.improperly_formatted_provisioning, Toast.LENGTH_LONG).show();
            return;
        }
        final Jid jid = accountConfiguration.getJid();
        final List<Jid> accounts = DatabaseBackend.getInstance(activity).getAccountJids(true);
        if (accounts.contains(jid)) {
            Toast.makeText(activity, R.string.account_already_exists, Toast.LENGTH_LONG).show();
            return;
        }
        final Intent serviceIntent = new Intent(activity, XmppConnectionService.class);
        serviceIntent.setAction(XmppConnectionService.ACTION_PROVISION_ACCOUNT);
        serviceIntent.putExtra("address", jid.asBareJid().toEscapedString());
        serviceIntent.putExtra("password", accountConfiguration.password);
        Compatibility.startService(activity, serviceIntent);
        final Intent intent = new Intent(activity, EditAccountActivity.class);
        intent.putExtra("jid", jid.asBareJid().toEscapedString());
        intent.putExtra("init", true);
        activity.startActivity(intent);
    }

}
