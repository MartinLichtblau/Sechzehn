package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.ChatUser;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Message;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentMessageBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.OutcomingMessageViewHolder;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.socket.ChatSocket;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.services.ChatNotificationService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.ApiMessage;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import retrofit2.Call;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.ChatService.ChatService;
import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.UserService.UserService;

/**
 * Provides the chat view to send and display messages.
 * <p>
 * A simple {@link Fragment} subclass.
 * Use the {@link MessageFragment#newInstance} factory method to
 * create an object of this fragment.
 */
public class MessageFragment extends DataBindingFragment<FragmentMessageBinding> {


    private static final String ARG_PARAM1 = "username";
    public static final int PER_PAGE = 10;

    private final ChatUser owner = new ChatUser(BottomTabsActivity.getOwnerViewModel().getOwner().getValue());
    private ChatSocket socket;
    private String username;
    private String ownername = SzUtils.getOwnername();
    @NonNull
    private CharSequence oldInput = "";
    private ChatUser user;
    private int totalItemCountServer;
    private MessagesListAdapter<Message> adapter;

    /**
     * Loads and draws the profile picture.
     */
    private ImageLoader imageLoader = new ImageLoader() {
        @Override
        public void loadImage(ImageView imageView, String url) {
            Picasso.with(getActivityEx()).load(url)
                    .placeholder(R.drawable.ic_owner) //Placeholders and error images are not resized and must be fairly small images.
                    .centerCrop().resize(40, 40)
                    .transform(SzUtils.CROP_CIRCLE_TRANSFORMATION).into(imageView);
        }
    };

    /**
     * Listener executed if the User scrolls and more Messages are needed.
     */
    private MessagesListAdapter.OnLoadMoreListener loadMoreListener = new MessagesListAdapter.OnLoadMoreListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            Log.d("ChatLoad", page + " " + totalItemsCount);
            if (totalItemsCount < totalItemCountServer) {
                loadMessages(page + 1);
            }
        }
    };
    /**
     * Listener executed if the User sends a Text.
     */
    private MessageInput.InputListener inputListener = new MessageInput.InputListener() {
        @Override
        public boolean onSubmit(final CharSequence input) {
            socket.sendMessage(new Message(SzUtils.getOwnername(), username, input.toString()));
            oldInput = input;
            return true;
        }
    };
    /**
     * Listener executed if a new Message arrives.
     */
    private ChatSocket.Listener messagesListener = new ChatSocket.Listener() {
        @Override
        public void call(final Message m) {
            ensureUsersAreLoaded(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (userOrOwnerMatch(m)) {
                                adapter.addToStart(m, true);
                                sendRead(m);
                            }
                        }
                    });
                }
            });
        }
    };
    /**
     * Listener executed if a {@link ChatSocket#EVENT_WARNING} occurs.
     */
    private ChatSocket.WarningListener warningListener = new ChatSocket.WarningListener() {
        @Override
        public void call(final ApiMessage err) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.messagesInput.getInputEditText().setText(oldInput);
                    Toast.makeText(getActivityEx(), err.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    };
    /**
     * Listener executed if a Message is read.
     */
    private ChatSocket.Listener readListener = new ChatSocket.Listener() {
        @Override
        public void call(final Message m) {
            ensureUsersAreLoaded(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (m.sender.equals(ownername)) {
                                m.senderUser = owner;
                                adapter.update(m);
                            }
                        }
                    });
                }
            });
        }
    };

    /**
     * Required empty public constructor.
     * <p>
     * Please use {@link #newInstance(String)} instead.
     */
    public MessageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new object of
     * this fragment using the provided parameters.
     *
     * @param username Username of the chat partner.
     * @return A new instance of fragment MessageFragment.
     */
    public static MessageFragment newInstance(String username) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, username);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    protected FragmentMessageBinding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMessageBinding.inflate(inflater, container, false);
    }

    @Override
    protected void useDataBinding(final FragmentMessageBinding binding, Bundle savedInstanceState) {
        MessageHolders holders = new MessageHolders();
        holders.setDateHeaderLayout(R.layout.message_item_date);
        holders.setOutcomingTextHolder(OutcomingMessageViewHolder.class);
        adapter = new MessagesListAdapter<>(SzUtils.getOwnername(), holders, imageLoader);
        adapter.setLoadMoreListener(loadMoreListener);

        if (socket == null) {
            socket = new ChatSocket();
            socket.addOnMessageListener(messagesListener);
            socket.addOnWarningListener(warningListener);
            socket.addOnReadListener(readListener);
        }
        socket.connect();

        ChatNotificationService.blockUser(username);
        binding.messagesList.setAdapter(adapter);
        binding.messagesInput.setInputListener(inputListener);
        loadMessages(1);


        ensureUsersAreLoaded(new Runnable() {
            @Override
            public void run() {
                binding.setUser(user.getUser());
            }
        });
    }

    public void showUser(View v) {
        fragNavController().pushFragment(UserProfileFragment.newInstance(username));
    }

    /**
     * Executes a Runnable after if the senderUser object is already present or retrieves the
     * senderUser object and executes the runnable afterwards.
     *
     * @param runnable Runnable executed after senderUser object is present.
     */
    private void ensureUsersAreLoaded(final Runnable runnable) {
        if (user != null) {
            runnable.run();
            return;
        }
        UserService.getUser(username)
                .enqueue(new DefaultCallback<User>(getActivityEx()) {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (!response.isSuccessful()) {
                            Log.w(TAG, "MessageFragment: User not loaded.");
                            return;
                        }
                        user = new ChatUser(response.body());
                        runnable.run();
                    }
                });
    }

    /**
     * Loads messages from server.
     *
     * @param page current page.
     */
    private void loadMessages(final int page) {
        ensureUsersAreLoaded(new Runnable() {
            @Override
            public void run() {
                ChatService.getMessages(username, page, PER_PAGE)
                        .enqueue(new DefaultCallback<Pagination<Message>>(getActivityEx()) {
                            @Override
                            public void onResponse(Call<Pagination<Message>> call, Response<Pagination<Message>> response) {
                                if (!response.isSuccessful()) {
                                    Log.w(TAG, "MessageFragment: loadMessages: Messages not loaded.");
                                    return;
                                }
                                processNewMessages(response);
                            }
                        });
            }
        });
    }

    /**
     * Adds the new Messages to the View and remove
     *
     * @param response Response Object containing new messages in a Pagination Object
     */
    private void processNewMessages(Response<Pagination<Message>> response) {
        totalItemCountServer = response.body().total;
        List<Message> messages = response.body().data;

        if (messages.isEmpty()) {
            return;
        }

        for (Message m : messages) {
            if (userOrOwnerMatch(m)) {
                sendRead(m);
            } else {
                messages.remove(m);
            }
        }
        adapter.addToEnd(messages, false);
    }

    /**
     * sends read receipt for a given message
     *
     * @param m the message a read receipt has to be send for
     */
    private void sendRead(Message m) {
        if (!m.isRead && m.receiver.equals(ownername)) {
            socket.sendMessageRead(m);
        }
    }

    /**
     * determine the message source
     *
     * @param m Message
     * @return true iff senderUser equals User
     */
    private boolean userOrOwnerMatch(Message m) {
        if (m.sender.equals(username) && m.receiver.equals(ownername)) {
            m.senderUser = user;
        } else if (m.sender.equals(ownername) && m.receiver.equals(username)) {
            m.senderUser = owner;
        } else {
            return false;
        }

        return true;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socket.disconnect();
        ChatNotificationService.unblockUser(username);
    }
}
