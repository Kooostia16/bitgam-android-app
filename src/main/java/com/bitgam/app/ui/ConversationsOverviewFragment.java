/*
 * Copyright (c) 2018, Daniel Gultsch All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.bitgam.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import androidx.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import com.bitgam.app.Config;
import com.bitgam.app.R;
import com.bitgam.app.databinding.FragmentConversationsOverviewBinding;
import com.bitgam.app.entities.Conversation;
import com.bitgam.app.entities.Conversational;
import com.bitgam.app.ui.adapter.ConversationAdapter;
import com.bitgam.app.ui.interfaces.OnConversationArchived;
import com.bitgam.app.ui.interfaces.OnConversationSelected;
import com.bitgam.app.ui.util.MenuDoubleTabUtil;
import com.bitgam.app.ui.util.PendingActionHelper;
import com.bitgam.app.ui.util.PendingItem;
import com.bitgam.app.ui.util.ScrollState;
import com.bitgam.app.ui.util.StyledAttributes;
import com.bitgam.app.utils.ThemeHelper;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public class ConversationsOverviewFragment extends XmppFragment {

	private static final String STATE_SCROLL_POSITION = ConversationsOverviewFragment.class.getName()+".scroll_state";

	private final List<Conversation> conversations = new ArrayList<>();
	public final List<Conversation> choosenConversations = new ArrayList<>();
	private final List<View> adapterViews = new ArrayList<>();
	private final PendingItem<Conversation> swipedConversation = new PendingItem<>();
	private final PendingItem<ScrollState> pendingScrollState = new PendingItem<>();
	private FragmentConversationsOverviewBinding binding;
	private ConversationAdapter conversationsAdapter;
	private XmppActivity activity;
	//private ConversationsActivity activity2;
	//private float mSwipeEscapeVelocity = 0f;
	private PendingActionHelper pendingActionHelper = new PendingActionHelper();
	private Conversation selectedConversation;
	public boolean toolbarMode = false; // Are we choosing conversations for delete/pin or not.


	/*private ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,LEFT|RIGHT) {
		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
			//todo maybe we can manually changing the position of the conversation
			return false;
		}

		@Override
		public float getSwipeEscapeVelocity (float defaultValue) {
			return mSwipeEscapeVelocity;
		}

		@Override
		public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
									float dX, float dY, int actionState, boolean isCurrentlyActive) {
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			if(actionState != ItemTouchHelper.ACTION_STATE_IDLE){
				Paint paint = new Paint();
				paint.setColor(StyledAttributes.getColor(activity,R.attr.conversations_overview_background));
				paint.setStyle(Paint.Style.FILL);
				c.drawRect(viewHolder.itemView.getLeft(),viewHolder.itemView.getTop()
						,viewHolder.itemView.getRight(),viewHolder.itemView.getBottom(), paint);
			}
		}

		@Override
		public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
			super.clearView(recyclerView, viewHolder);
			viewHolder.itemView.setAlpha(1f);
		}

		@Override
		public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
			pendingActionHelper.execute();
			int position = viewHolder.getLayoutPosition();
			try {
				swipedConversation.push(conversations.get(position));
			} catch (IndexOutOfBoundsException e) {
				return;
			}
			conversationsAdapter.remove(swipedConversation.peek(), position);
			activity.xmppConnectionService.markRead(swipedConversation.peek());

			if (position == 0 && conversationsAdapter.getItemCount() == 0) {
				final Conversation c = swipedConversation.pop();
				activity.xmppConnectionService.archiveConversation(c);
				return;
			}
			final boolean formerlySelected = ConversationFragment.getConversation(getActivity()) == swipedConversation.peek();
			if (activity instanceof OnConversationArchived) {
				((OnConversationArchived) activity).onConversationArchived(swipedConversation.peek());
			}
			final Conversation c = swipedConversation.peek();
			final int title;
			if (c.getMode() == Conversational.MODE_MULTI) {
				if (c.getMucOptions().isPrivateAndNonAnonymous()) {
					title = R.string.title_undo_swipe_out_group_chat;
				} else {
					title = R.string.title_undo_swipe_out_channel;
				}
			} else {
				title = R.string.title_undo_swipe_out_conversation;
			}

			final Snackbar snackbar =
					Snackbar.make(binding.list, title, 5000)
					.setAction(R.string.undo, v -> {
						pendingActionHelper.undo();
						Conversation conversation = swipedConversation.pop();
						conversationsAdapter.insert(conversation, position);
						if (formerlySelected) {
							if (activity instanceof OnConversationSelected) {
								((OnConversationSelected) activity).onConversationSelected(c);
							}
						}
						LinearLayoutManager layoutManager = (LinearLayoutManager) binding.list.getLayoutManager();
						if (position > layoutManager.findLastVisibleItemPosition()) {
							binding.list.smoothScrollToPosition(position);
						}
					})
					.addCallback(new Snackbar.Callback() {
						@Override
						public void onDismissed(Snackbar transientBottomBar, int event) {
							switch (event) {
								case DISMISS_EVENT_SWIPE:
								case DISMISS_EVENT_TIMEOUT:
									pendingActionHelper.execute();
									break;
							}
						}
					});

			pendingActionHelper.push(() -> {
				if (snackbar.isShownOrQueued()) {
					snackbar.dismiss();
				}
				final Conversation conversation = swipedConversation.pop();
				if(conversation != null){
					if (!conversation.isRead() && conversation.getMode() == Conversation.MODE_SINGLE) {
						return;
					}
					activity.xmppConnectionService.archiveConversation(c);
				}
			});

			ThemeHelper.fix(snackbar);
			snackbar.show();
		}
	};

	private ItemTouchHelper touchHelper = new ItemTouchHelper(callback);*/

	public static Conversation getSuggestion(Activity activity) {
		final Conversation exception;
		Fragment fragment = activity.getFragmentManager().findFragmentById(R.id.main_fragment);
		if (fragment instanceof ConversationsOverviewFragment) {
			exception = ((ConversationsOverviewFragment) fragment).swipedConversation.peek();
		} else {
			exception = null;
		}
		return getSuggestion(activity, exception);
	}

	public static Conversation getSuggestion(Activity activity, Conversation exception) {
		Fragment fragment = activity.getFragmentManager().findFragmentById(R.id.main_fragment);
		if (fragment instanceof ConversationsOverviewFragment) {
			List<Conversation> conversations = ((ConversationsOverviewFragment) fragment).conversations;
			if (conversations.size() > 0) {
				Conversation suggestion = conversations.get(0);
				if (suggestion == exception) {
					if (conversations.size() > 1) {
						return conversations.get(1);
					}
				} else {
					return suggestion;
				}
			}
		}
		return null;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null) {
			return;
		}
		pendingScrollState.push(savedInstanceState.getParcelable(STATE_SCROLL_POSITION));
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof XmppActivity) {
			this.activity = (XmppActivity) activity;
			//this.activity2 = (ConversationsActivity) activity;
		} else {
			throw new IllegalStateException("Trying to attach fragment to activity that is not an XmppActivity");
		}
	}

	@Override
	public void onPause() {
		Log.d(Config.LOGTAG,"ConversationsOverviewFragment.onPause()");
		pendingActionHelper.execute();
		super.onPause();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.activity = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//this.mSwipeEscapeVelocity = getResources().getDimension(R.dimen.swipe_escape_velocity);
		this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_conversations_overview, container, false);
		this.binding.fab.setOnClickListener((view) -> StartConversationActivity.launch(getActivity()));
		this.conversationsAdapter = new ConversationAdapter(this.activity, this.conversations, this.choosenConversations, this.adapterViews);

		this.conversationsAdapter.setConversationClickListener((view, conversation) -> {
			if (activity instanceof OnConversationSelected) {
					((OnConversationSelected) activity).onConversationSelected(conversation);
			} else {
				Log.w(ConversationsOverviewFragment.class.getCanonicalName(), "Activity does not implement OnConversationSelected");
			}
		});

		this.conversationsAdapter.setConversationClickLongListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (!toolbarMode) {
					toolbarMode = true;
					conversationsAdapter.choosing = true;
					activity.invalidateOptionsMenu();
					((ConversationsActivity )activity).invalidateActionBar();
					return true;
				}

				return false;
			}
		});

		this.binding.list.setAdapter(this.conversationsAdapter);
		this.binding.list.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
		//this.touchHelper.attachToRecyclerView(this.binding.list);
		registerForContextMenu(this.binding.list);
		return binding.getRoot();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		if (!toolbarMode) {
			menuInflater.inflate(R.menu.fragment_conversations_overview, menu);
		} else {
			menuInflater.inflate(R.menu.fragment_conversations_overview_choose, menu);
		}
	}

	/*@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		Log.d(Config.LOGTAG,"OnOptionsMenu ");
		synchronized (this.conversations) {
			super.onCreateContextMenu(menu, v, menuInfo);
			//AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
			//this.selectedConversation = v.position;//this.conversations.get(acmi.position);
			populateContextMenu(menu);
		}
	}

	private void populateContextMenu(ContextMenu menu) {
		//final Conversation m = this.selectedConversation;
		Log.d(Config.LOGTAG,"OnOptionsMenu create");
		activity.getMenuInflater().inflate(R.menu.message_context, menu);
		menu.setHeaderTitle("Conversation options");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case 0:
				((OnConversationSelected) activity).onConversationSelected(this.conversations.get(item.getGroupId()));
				break;
			case 1:

				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			default:
				break;
		}
		return super.onContextItemSelected(item);
	}*/

	@Override
	public void onBackendConnected() {
		refresh();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		ScrollState scrollState = getScrollState();
		if (scrollState != null) {
			bundle.putParcelable(STATE_SCROLL_POSITION, scrollState);
		}
	}

	private ScrollState getScrollState() {
		if (this.binding == null) {
			return null;
		}
		LinearLayoutManager layoutManager = (LinearLayoutManager) this.binding.list.getLayoutManager();
		int position = layoutManager.findFirstVisibleItemPosition();
		final View view = this.binding.list.getChildAt(0);
		if (view != null) {
			return new ScrollState(position,view.getTop());
		} else {
			return new ScrollState(position, 0);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(Config.LOGTAG, "ConversationsOverviewFragment.onStart()");
		if (activity.xmppConnectionService != null) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(Config.LOGTAG, "ConversationsOverviewFragment.onResume()");
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (MenuDoubleTabUtil.shouldIgnoreTap()) {
			return false;
		}

		if (!toolbarMode) {
			switch (item.getItemId()) {
				case R.id.action_search:
					startActivity(new Intent(getActivity(), SearchActivity.class));
					return true;
			}
		} else {
			switch(item.getItemId()) {
				case R.id.pin_conversation:
					for (Conversation c : choosenConversations) {
						final boolean pinned = c.getBooleanAttribute(Conversation.ATTRIBUTE_PINNED_ON_TOP, false);
						c.setAttribute(Conversation.ATTRIBUTE_PINNED_ON_TOP, !pinned);
						activity.xmppConnectionService.updateConversation(c);
						activity.invalidateOptionsMenu();
					}
					toolbarMode = false;
					conversationsAdapter.choosing = false;
					activity.invalidateOptionsMenu();
					((ConversationsActivity) activity).invalidateActionBar();
					this.choosenConversations.removeAll(this.choosenConversations);
					this.conversationsAdapter.notifyDataSetChanged();
					break;
				case R.id.remove_conversation:
					for (Conversation c :choosenConversations) {
						activity.xmppConnectionService.archiveConversation(c);
					}
					break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	public void onChooseConversationsToolbar(MenuItem item) {

			if (item.getItemId() == android.R.id.home) {
				toolbarMode = false;
				conversationsAdapter.choosing = false;
				activity.invalidateOptionsMenu();
				((ConversationsActivity) activity).invalidateActionBar();
				this.conversationsAdapter.notifyDataSetChanged();
				this.choosenConversations.removeAll(this.choosenConversations);
		}
	}

	@Override
	void refresh() {
		if (this.binding == null || this.activity == null) {
			Log.d(Config.LOGTAG,"ConversationsOverviewFragment.refresh() skipped updated because view binding or activity was null");
			return;
		}
		this.activity.xmppConnectionService.populateWithOrderedConversations(this.conversations);

		Conversation removed = this.swipedConversation.peek();
		if (removed != null) {
			if (removed.isRead()) {
				this.conversations.remove(removed);
			} else {
				pendingActionHelper.execute();
			}
		}
		this.conversationsAdapter.notifyDataSetChanged();
		ScrollState scrollState = pendingScrollState.pop();
		if (scrollState != null) {
			setScrollPosition(scrollState);
		}
	}

	private void setScrollPosition(ScrollState scrollPosition) {
		if (scrollPosition != null) {
			LinearLayoutManager layoutManager = (LinearLayoutManager) binding.list.getLayoutManager();
			layoutManager.scrollToPositionWithOffset(scrollPosition.position, scrollPosition.offset);
		}
	}
}
