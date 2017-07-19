package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.FriendItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentFriendsBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.ServiceGenerator;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.Services.UserService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by niccapdevila on 3/26/16.
 */
public class FriendsFragment extends DataBindingFragment<FragmentFriendsBinding> implements SearchView.OnQueryTextListener, FastAdapter.OnClickListener<FriendItem> {

    //save our FastAdapter
    private FastItemAdapter<FriendItem> fastItemAdapter;
    private FooterAdapter<ProgressItem> footerAdapter;


    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    protected FragmentFriendsBinding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFriendsBinding.inflate(inflater, container, false);
    }

    @Override
    protected void useDataBinding(FragmentFriendsBinding binding) {
        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withOnClickListener(this);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        binding.friendsList.setLayoutManager(llm);
        binding.friendsList.setAdapter(fastItemAdapter);

        binding.friendsSearch.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        ServiceGenerator.createService(UserService.class, SzUtils.getToken()).getUsers(1, 10, null, null, null, null, query).enqueue(new Callback<Pagination<User>>() {
            @Override
            public void onResponse(Call<Pagination<User>> call, Response<Pagination<User>> response) {
                if (response.isSuccessful()) {
                    fastItemAdapter.clear();
                    for (User u : response.body().data) {
                        fastItemAdapter.add(FriendItem.create(u));
                    }
                }

            }

            @Override
            public void onFailure(Call<Pagination<User>> call, Throwable t) {
                Toast.makeText(getActivity(), "Connectivity error!", Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }

    public boolean onClick(View v, IAdapter<FriendItem> adapter, FriendItem item, int position) {
        fragNavController().pushFragment(UserProfileFragment.newInstance(item.username));
        return true;
    }
}
