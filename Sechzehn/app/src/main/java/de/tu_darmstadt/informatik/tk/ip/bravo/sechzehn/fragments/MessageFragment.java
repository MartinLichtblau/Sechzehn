package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.net.URISyntaxException;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Message;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentMessageBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.ChatService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
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

    private Socket socket;

    private String username;


    private ImageLoader imageLoader = new ImageLoader() {
        @Override
        public void loadImage(ImageView imageView, String url) {
            Picasso.with(getActivity()).load(url).into(imageView);
        }
    };

    private final MessagesListAdapter<Message> adapter = new MessagesListAdapter<>(username, imageLoader);

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
        public boolean onSubmit(CharSequence input) {
            socket.emit("message", new Object[]{new Message(SzUtils.getOwnername(), username, input.toString())}, new Ack() {
                @Override
                public void call(Object... args) {
                    if (args.length == 1) {
                        Message m = SzUtils.gson.fromJson(args[0].toString(), Message.class);
                        adapter.addToStart(m, true);
                    }
                }
            }).on("warning", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Toast.makeText(getActivity(), args[0].toString(), Toast.LENGTH_SHORT).show();

                }
            });
            return true;
        }
    };

    @Override
    protected void useDataBinding(FragmentMessageBinding binding) {
        IO.Options options = new IO.Options();
        options.query = "token=" + SzUtils.getToken();
        try {
            socket = IO.socket("https://iptk.herokuapp.com/messages", options);
            socket.on("error", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    throw new RuntimeException(args[0].toString());
                }
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        adapter.setLoadMoreListener(new MessagesListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                ServiceGenerator.createService(ChatService.class).getMessages(
                        SzUtils.getOwnername(), page, totalItemsCount
                ).enqueue(new DefaultCallback<Pagination<Message>>(getActivity()) {
                              @Override
                              public void onResponse(Call<Pagination<Message>> call, Response<Pagination<Message>> response) {
                                  adapter.addToEnd(response.body().data, false);
                              }
                          }
                );
            }
        });
        binding.messagesList.setAdapter(adapter);
        binding.messagesInput.setInputListener(inputListener);
    }
}
