package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;

import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.FriendItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentFriendsBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.viewModels.FriendsViewModel;

/**
 * Created by niccapdevila on 3/26/16.
 */
public class FriendsFragment extends BaseFragment {
    private FragmentFriendsBinding binding;
    private FriendsViewModel viewModel;
    //save our FastAdapter
    private FastItemAdapter<FriendItem> fastItemAdapter;
    private FooterAdapter<ProgressItem> footerAdapter;


    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(FriendsViewModel.class);
        viewModel.initialize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friends, container, false);
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //create our FastAdapter which will manage everything
        fastItemAdapter  = new FastItemAdapter();
        fastItemAdapter .withSelectable(true);

        //set our adapters to the RecyclerView
        //we wrap our FastAdapter inside the ItemAdapter -> This allows us to chain adapters for more complex useCases
        recyclerView.setAdapter(fastItemAdapter);

        //create our FooterAdapter which will manage the progress items
        footerAdapter = new FooterAdapter<>();

        //configure our fastAdapter
        fastItemAdapter.withOnClickListener(new FastAdapter.OnClickListener<FriendItem>() {
            @Override
            public boolean onClick(View v, IAdapter<FriendItem> adapter, FriendItem item, int position) {
                //Toast.makeText(v.getContext(), (item).name.getText(v.getContext()), Toast.LENGTH_LONG).show();
                return false;
            }
        });

        //set the items to your ItemAdapter
        List<User> friendList = viewModel.getFriends(null, null).observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(@Nullable List<User> users) {
                fastItemAdapter.add(users); //@TODO retrieve data from viewModel
            }
        });



        return binding.getRoot();
    }

}
