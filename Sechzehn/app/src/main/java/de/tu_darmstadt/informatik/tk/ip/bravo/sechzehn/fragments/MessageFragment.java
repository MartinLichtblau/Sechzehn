package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Message;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentMessageBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageFragment extends DataBindingFragment<FragmentMessageBinding> {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "username";



    private String username;

    ImageLoader imageLoader = new ImageLoader() {
        @Override
        public void loadImage(ImageView imageView, String url) {
            Picasso.with(getActivity()).load(url).into(imageView);
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
        return FragmentMessageBinding.inflate(inflater,container,false);
    }

    @Override
    protected void useDataBinding(FragmentMessageBinding binding) {
        final MessagesListAdapter<Message> adapter = new MessagesListAdapter<>(username, imageLoader);
        adapter.setLoadMoreListener(new MessagesListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                //TODO
            }
        });
        binding.messagesList.setAdapter(adapter);
    }
}
