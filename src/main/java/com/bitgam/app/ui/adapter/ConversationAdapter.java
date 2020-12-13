package com.bitgam.app.ui.adapter;

import androidx.databinding.DataBindingUtil;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bitgam.app.ui.ConversationsOverviewFragment;
import com.google.common.base.Optional;

import java.util.List;

import com.bitgam.app.R;
import com.bitgam.app.databinding.ConversationListRowBinding;
import com.bitgam.app.entities.Conversation;
import com.bitgam.app.entities.Conversational;
import com.bitgam.app.entities.Message;
import com.bitgam.app.ui.ConversationFragment;
import com.bitgam.app.ui.XmppActivity;
import com.bitgam.app.ui.util.AvatarWorkerTask;
import com.bitgam.app.ui.util.StyledAttributes;
import com.bitgam.app.utils.EmojiWrapper;
import com.bitgam.app.utils.IrregularUnicodeDetector;
import com.bitgam.app.utils.UIHelper;
import com.bitgam.app.xmpp.jingle.OngoingRtpSession;
import com.bitgam.app.xmpp.Jid;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private XmppActivity activity;
    private List<Conversation> conversations;
    private OnConversationClickListener listener;
    private View.OnLongClickListener longListener;
    private List<Conversation> conversationsChoosen;
    private List<View> adapterViews;
    public boolean choosing = false;

    public ConversationAdapter(XmppActivity activity, List<Conversation> conversations, List<Conversation> listConvs, List<View> listViews) {
        this.activity = activity;
        this.conversations = conversations;
        this.conversationsChoosen = listConvs;
        this.adapterViews = listViews;
    }

    public ConversationAdapter(XmppActivity activity, List<Conversation> conversations) {
        this.activity = activity;
        this.conversations = conversations;
    }


    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.conversation_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder viewHolder, int position) {
        Conversation conversation = conversations.get(position);
        if (conversation == null) {
            return;
        }
        CharSequence name = conversation.getName();
        if (name instanceof Jid) {
            viewHolder.binding.conversationName.setText(IrregularUnicodeDetector.style(activity, (Jid) name));
        } else {
            viewHolder.binding.conversationName.setText(EmojiWrapper.transform(name));
        }

        if (conversation == ConversationFragment.getConversation(activity)) {
            viewHolder.binding.frame.setBackgroundColor(StyledAttributes.getColor(activity, R.attr.color_background_tertiary));
        } else {
            viewHolder.binding.frame.setBackgroundColor(StyledAttributes.getColor(activity, R.attr.color_background_primary));
        }

        Message message = conversation.getLatestMessage();
        final int unreadCount = conversation.unreadCount();
        final boolean isRead = conversation.isRead();
        final Conversation.Draft draft = isRead ? conversation.getDraft() : null;
        if (unreadCount > 0) {
            viewHolder.binding.unreadCount.setVisibility(View.VISIBLE);
            viewHolder.binding.unreadCount.setUnreadCount(unreadCount);
        } else {
            viewHolder.binding.unreadCount.setVisibility(View.GONE);
        }

        if (isRead) {
            viewHolder.binding.conversationName.setTypeface(null, Typeface.NORMAL);
        } else {
            viewHolder.binding.conversationName.setTypeface(null, Typeface.BOLD);
        }

        if (draft != null) {
            viewHolder.binding.conversationLastmsgImg.setVisibility(View.GONE);
            viewHolder.binding.conversationLastmsg.setText(EmojiWrapper.transform(draft.getMessage()));
            viewHolder.binding.senderName.setText(R.string.draft);
            viewHolder.binding.senderName.setVisibility(View.VISIBLE);
            viewHolder.binding.conversationLastmsg.setTypeface(null, Typeface.NORMAL);
            viewHolder.binding.senderName.setTypeface(null, Typeface.ITALIC);
        } else {
            final boolean fileAvailable = !message.isDeleted();
            final boolean showPreviewText;
            if (fileAvailable && (message.isFileOrImage() || message.treatAsDownloadable() || message.isGeoUri())) {
                final int imageResource;
                if (message.isGeoUri()) {
                    imageResource = activity.getThemeResource(R.attr.ic_attach_location, R.drawable.ic_attach_location);
                    showPreviewText = false;
                } else {
                    //TODO move this into static MediaPreview method and use same icons as in MediaAdapter
                    final String mime = message.getMimeType();
                    switch (mime == null ? "" : mime.split("/")[0]) {
                        case "image":
                            imageResource = activity.getThemeResource(R.attr.ic_attach_photo, R.drawable.ic_attach_photo);
                            showPreviewText = false;
                            break;
                        case "video":
                            imageResource = activity.getThemeResource(R.attr.ic_attach_videocam, R.drawable.ic_attach_videocam);
                            showPreviewText = false;
                            break;
                        case "audio":
                            imageResource = activity.getThemeResource(R.attr.ic_attach_record, R.drawable.ic_attach_record);
                            showPreviewText = false;
                            break;
                        default:
                            imageResource = activity.getThemeResource(R.attr.ic_attach_document, R.drawable.ic_attach_document);
                            showPreviewText = true;
                            break;
                    }
                }
                viewHolder.binding.conversationLastmsgImg.setImageResource(imageResource);
                viewHolder.binding.conversationLastmsgImg.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.conversationLastmsgImg.setVisibility(View.GONE);
                showPreviewText = true;
            }
            final Pair<CharSequence, Boolean> preview = UIHelper.getMessagePreview(activity, message, viewHolder.binding.conversationLastmsg.getCurrentTextColor());
            if (showPreviewText) {
                viewHolder.binding.conversationLastmsg.setText(EmojiWrapper.transform(UIHelper.shorten(preview.first)));
            } else {
                viewHolder.binding.conversationLastmsgImg.setContentDescription(preview.first);
            }
            viewHolder.binding.conversationLastmsg.setVisibility(showPreviewText ? View.VISIBLE : View.GONE);
            if (preview.second) {
                if (isRead) {
                    viewHolder.binding.conversationLastmsg.setTypeface(null, Typeface.ITALIC);
                    viewHolder.binding.senderName.setTypeface(null, Typeface.NORMAL);
                } else {
                    viewHolder.binding.conversationLastmsg.setTypeface(null, Typeface.BOLD_ITALIC);
                    viewHolder.binding.senderName.setTypeface(null, Typeface.BOLD);
                }
            } else {
                if (isRead) {
                    viewHolder.binding.conversationLastmsg.setTypeface(null, Typeface.NORMAL);
                    viewHolder.binding.senderName.setTypeface(null, Typeface.NORMAL);
                } else {
                    viewHolder.binding.conversationLastmsg.setTypeface(null, Typeface.BOLD);
                    viewHolder.binding.senderName.setTypeface(null, Typeface.BOLD);
                }
            }
            if (message.getStatus() == Message.STATUS_RECEIVED) {
                if (conversation.getMode() == Conversation.MODE_MULTI) {
                    viewHolder.binding.senderName.setVisibility(View.VISIBLE);
                    viewHolder.binding.senderName.setText(UIHelper.getMessageDisplayName(message).split("\\s+")[0] + ':');
                } else {
                    viewHolder.binding.senderName.setVisibility(View.GONE);
                }
            } else if (message.getType() != Message.TYPE_STATUS) {
                viewHolder.binding.senderName.setVisibility(View.VISIBLE);
                viewHolder.binding.senderName.setText(activity.getString(R.string.me) + ':');
            } else {
                viewHolder.binding.senderName.setVisibility(View.GONE);
            }
        }


        final Optional<OngoingRtpSession> ongoingCall;
        if (conversation.getMode() == Conversational.MODE_MULTI) {
            ongoingCall = Optional.absent();
        } else {
            if (conversation.getContact().isActive()) {
                viewHolder.binding.isOnlineInfo.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.isOnlineInfo.setVisibility(View.GONE);
            }
            ongoingCall = activity.xmppConnectionService.getJingleConnectionManager().getOngoingRtpConnection(conversation.getContact());
        }

        if (ongoingCall.isPresent()) {
            viewHolder.binding.notificationStatus.setVisibility(View.VISIBLE);
                final int ic_ongoing_call = activity.getThemeResource(R.attr.ic_ongoing_call_hint, R.drawable.ic_phone_in_talk_black_18dp);
                viewHolder.binding.notificationStatus.setImageResource(ic_ongoing_call);
        } else {
            final long muted_till = conversation.getLongAttribute(Conversation.ATTRIBUTE_MUTED_TILL, 0);
            if (muted_till == Long.MAX_VALUE) {
                viewHolder.binding.notificationStatus.setVisibility(View.VISIBLE);
                int ic_notifications_off = activity.getThemeResource(R.attr.icon_notifications_off, R.drawable.ic_notifications_off_black_24dp);
                viewHolder.binding.notificationStatus.setImageResource(ic_notifications_off);
            } else if (muted_till >= System.currentTimeMillis()) {
                viewHolder.binding.notificationStatus.setVisibility(View.VISIBLE);
                int ic_notifications_paused = activity.getThemeResource(R.attr.icon_notifications_paused, R.drawable.ic_notifications_paused_black_24dp);
                viewHolder.binding.notificationStatus.setImageResource(ic_notifications_paused);
            } else if (conversation.alwaysNotify()) {
                viewHolder.binding.notificationStatus.setVisibility(View.GONE);
            } else {
                viewHolder.binding.notificationStatus.setVisibility(View.VISIBLE);
                int ic_notifications_none = activity.getThemeResource(R.attr.icon_notifications_none, R.drawable.ic_notifications_none_black_24dp);
                viewHolder.binding.notificationStatus.setImageResource(ic_notifications_none);
            }
        }

        long timestamp;
        if (draft != null) {
            timestamp = draft.getTimestamp();
        } else {
            timestamp = conversation.getLatestMessage().getTimeSent();
        }
        viewHolder.binding.pinnedOnTop.setVisibility(conversation.getBooleanAttribute(Conversation.ATTRIBUTE_PINNED_ON_TOP,false) ? View.VISIBLE : View.GONE);
        viewHolder.binding.conversationLastupdate.setText(UIHelper.readableTimeDifference(activity, timestamp));
        AvatarWorkerTask.loadAvatar(conversation, viewHolder.binding.conversationImage, R.dimen.avatar_on_conversation_overview);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (choosing) {
                   /* if (conversationsChoosen.get(position)) {
                        viewHolder.binding.imageView.setVisibility(View.GONE);
                        if (adapterViews.contains(viewHolder.binding.imageView)) {
                            adapterViews.remove(viewHolder.binding.imageView);
                        }
                    } else {
                        viewHolder.binding.imageView.setVisibility(View.VISIBLE);
                        if (!adapterViews.contains(viewHolder.binding.imageView)) {
                            adapterViews.add(viewHolder.binding.imageView);
                        }
                    }*/
                    if (conversationsChoosen.contains(conversation)) {
                        viewHolder.binding.imageView.setVisibility(View.GONE);
                        viewHolder.binding.conversationLastupdate.setVisibility(View.VISIBLE);
                        conversationsChoosen.remove(conversation);
                    } else {
                        viewHolder.binding.conversationLastupdate.setVisibility(View.GONE);
                        viewHolder.binding.imageView.setVisibility(View.VISIBLE);
                        conversationsChoosen.add(conversation);
                    }

                    return;
                }

                listener.onConversationClick(v,conversation);

            }
        });

        if (!conversationsChoosen.contains(conversation)) {
            viewHolder.binding.imageView.setVisibility(View.GONE);
        } else {
            viewHolder.binding.imageView.setVisibility(View.VISIBLE);
        }

        viewHolder.itemView.setOnLongClickListener(longListener);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void setConversationClickListener(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void setConversationClickLongListener(View.OnLongClickListener listener) {
        this.longListener = listener;
    }


    public void insert(Conversation c, int position) {
        conversations.add(position, c);
        notifyDataSetChanged();
    }

    public void remove(Conversation conversation, int position) {
        conversations.remove(conversation);
        notifyItemRemoved(position);
    }

    public interface OnConversationClickListener {
        void onConversationClick(View view, Conversation conversation);
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        private final ConversationListRowBinding binding;
        public final int position;

        private ConversationViewHolder(ConversationListRowBinding binding) {
            super(binding.getRoot());
            position = this.getAdapterPosition();
            this.binding = binding;
            this.binding.getRoot().setOnCreateContextMenuListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            /*menu.setHeaderTitle("Select The Action");

            int indexOfItemClickedInList = getAdapterPosition();

            menu.add(indexOfItemClickedInList, 0, 0, "Open conversation");
            menu.add(indexOfItemClickedInList, 1, 0, "---------------------");
            menu.add(indexOfItemClickedInList, 2, 0, "Add to private folder");
            menu.add(indexOfItemClickedInList, 3, 0, "Add to favourites folder");
            menu.add(indexOfItemClickedInList, 4, 0, "Remove from this folder");*/
        }
    }

}
