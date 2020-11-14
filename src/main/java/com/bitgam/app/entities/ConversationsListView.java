package com.bitgam.app.entities;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;

import com.bitgam.app.Config;

public class ConversationsListView  extends RecyclerView implements View.OnCreateContextMenuListener {

    public ConversationsListView(Context context) {
        super(context);
        //this.setOnCreateContextMenuListener(this);
    }

    public ConversationsListView(Context context, AttributeSet attrs) {
        super(context,attrs);
        //this.setOnCreateContextMenuListener(this);
    }

    public ConversationsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        //context.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(Config.LOGTAG,"OnOptionsMenu create");
        menu.setHeaderTitle("Test options");
        menu.add("Item 1");
        menu.add("Item 2");
    }
}
