package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
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
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.ChatService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.UserService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.socket.ChatSocket;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.APIError;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import io.socket.emitter.Emitter;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import retrofit2.Call;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageFragment extends DataBindingFragment<FragmentMessageBinding> {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "username";
    public static final ChatService CHAT_SERVICE = ServiceGenerator.createService(ChatService.class);

    private ChatSocket socket;

    private String username;

    private ChatUser user;
    private final ChatUser owner = new ChatUser(BottomTabsActivity.getOwnerViewModel().getOwner().getValue());

    private int totalItemCountServer;
    private MessagesListAdapter<Message> adapter;

    private ImageLoader imageLoader = new ImageLoader() {
        @Override
        public void loadImage(ImageView imageView, String url) {
            Picasso.with(getActivity()).load(url)
                    .placeholder(R.drawable.ic_owner) //Placeholders and error images are not resized and must be fairly small images.
                    .centerCrop().resize(40, 40)
                    .transform(new CropCircleTransformation()).into(imageView);
        }
    };


    private MessagesListAdapter.OnLoadMoreListener loadMoreListener = new MessagesListAdapter.OnLoadMoreListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            Log.d("ChatLoad", page + " " + totalItemsCount);
            if (totalItemsCount < totalItemCountServer) {
                loadMessages(page + 1, 10);
            }
        }
    };


    public MessageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MessageFragment.
     */

    public static MessageFragment newInstance(String param1) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
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


    private MessageInput.InputListener inputListener = new MessageInput.InputListener() {
        @Override
        public boolean onSubmit(final CharSequence input) {
            socket.sendMessage(new Message(SzUtils.getOwnername(), username, input.toString()))
                    .on(ChatSocket.EVENT_WARNING, new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.messagesInput.getInputEditText().setText(input);
                                    APIError err = APIError.fromJson(args[0].toString());
                                    Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });
            return true;
        }
    };

    ChatSocket.Listener messagesListener = new ChatSocket.Listener() {
        @Override
        public void call(final Message m) {
            ensureUsersAreLoaded(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addUserToMessage(m);
                            adapter.addToStart(m, true);
                        }
                    });
                }
            });

        }
    };

    @Override
    protected void useDataBinding(final FragmentMessageBinding binding) {
        adapter = new MessagesListAdapter<>(SzUtils.getOwnername(), imageLoader);
        socket = new ChatSocket();

        adapter.setLoadMoreListener(loadMoreListener);
        socket.setOnMessageListener(messagesListener);

        binding.messagesList.setAdapter(adapter);
        binding.messagesInput.setInputListener(inputListener);
        loadMessages(1, 10);

        ensureUsersAreLoaded(new Runnable() {
            @Override
            public void run() {
                binding.messageToolbar.setTitle(user.getUser().getOptionalRealName());
                binding.messageToolbar.setSubtitle(user.getUser().getUsername());
            }
        });
    }


    private void ensureUsersAreLoaded(final Runnable runnable) {
        if (user != null) {
            runnable.run();
            return;
        }
        ServiceGenerator.createService(UserService.class)
                .getUser(username)
                .enqueue(new DefaultCallback<User>(getActivity()) {
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

    private void loadMessages(final int page, final int per_page) {
        ensureUsersAreLoaded(new Runnable() {
            @Override
            public void run() {
                CHAT_SERVICE.getMessages(username, page, per_page)
                        .enqueue(new DefaultCallback<Pagination<Message>>(getActivity()) {
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

    private void processNewMessages(Response<Pagination<Message>> response) {


        totalItemCountServer = response.body().total;
        List<Message> messages = response.body().data;

        if (messages.isEmpty()) {
            return;
        }
        for (Message m : messages) {
            addUserToMessage(m);
        }
        adapter.addToEnd(messages, false);
    }

    private void addUserToMessage(Message m) {
        if (m.sender.equals(username)) {
            m.user = user;
        } else {
            m.user = owner;
        }
    }
}
