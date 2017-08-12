package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter.adapters.HeaderAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;

import java.util.LinkedList;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.HeaderItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.UserItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentFriendsBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.FriendshipService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.UserService;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.DefaultCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.FriendshipService.FriendshipService;
import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.UserService.UserService;


/**
 * Created by niccapdevila on 3/26/16.
 */
public class FriendsFragment extends DataBindingFragment<FragmentFriendsBinding> implements SearchView.OnQueryTextListener, FastAdapter.OnClickListener<IItem> {

    private static final List<User> EMPTY_USERS = new LinkedList<User>();
    private static final List<Friendship> EMPTY_FRIENDSHIP_REQUESTS = new LinkedList<>();
    //save our FastAdapter
    FastItemAdapter<IItem> fastAdapter = new FastItemAdapter<>();
    // Head is a model class for your header
    private HeaderAdapter<HeaderItem> headerAdapter = new HeaderAdapter<>();
    private FooterAdapter<ProgressItem> footerAdapter = new FooterAdapter<>();
    private List<User> users = null;
    private List<User> friends = null;
    private List<Friendship> friendshipRequests = null;
    private final Object sync = new Object();
    private List<User> friendsInList = new LinkedList<>();
    private List<User> usersInList = new LinkedList<>();
    private List<Friendship> friendshipRequestsInList = new LinkedList<>();

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    protected FragmentFriendsBinding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFriendsBinding.inflate(inflater, container, false);
    }

    @Override
    protected void useDataBinding(final FragmentFriendsBinding binding) {

        fastAdapter.withOnClickListener(this);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        binding.friendsList.setLayoutManager(llm);
        binding.friendsList.setAdapter(
                headerAdapter.wrap(fastAdapter));

        binding.friendsSearch.setOnQueryTextListener(this);
        // onQueryTextChange(binding.friendsSearch.getQuery().toString());

    }

    @Override
    public void onStart() {
        super.onStart();
        if(binding.friendsSearch.getQuery().toString().isEmpty()){
            onQueryTextChange("");
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if (newText.isEmpty()) {
            UserService.getFriends(SzUtils.getOwnername(), 1, 10)
                    .enqueue(new DefaultCallback<Pagination<User>>(getActivity()) {
                        @Override
                        public void onResponse(Call<Pagination<User>> call, Response<Pagination<User>> response) {
                            if (response.isSuccessful()) {
                                friends = response.body().data;
                                users = EMPTY_USERS;
                                updateUserList();
                            }
                        }
                    });
            FriendshipService.getFriendshipRequests(SzUtils.getOwnername(), 1, 10)
                    .enqueue(new DefaultCallback<Pagination<Friendship>>(getActivity()) {
                        @Override
                        public void onResponse(Call<Pagination<Friendship>> call, Response<Pagination<Friendship>> response) {
                            if (response.isSuccessful()) {
                                friendshipRequests = response.body().data;
                                users = EMPTY_USERS;
                                updateUserList();
                            }
                        }
                    });

        } else {
            UserService.getUsers(1, 10, null, null, null, false, newText)
                    .enqueue(new DefaultCallback<Pagination<User>>(getActivity()) {
                        @Override
                        public void onResponse(Call<Pagination<User>> call, Response<Pagination<User>> response) {
                            if (response.isSuccessful()) {
                                users = response.body().data;
                                friendshipRequests = EMPTY_FRIENDSHIP_REQUESTS;
                                updateUserList();
                            }
                        }
                    });
            UserService.getUsers(1, 10, null, null, null, true, newText)
                    .enqueue(new DefaultCallback<Pagination<User>>(getActivity()) {
                                 @Override
                                 public void onResponse(Call<Pagination<User>> call, Response<Pagination<User>> response) {
                                     if (response.isSuccessful()) {
                                         friends = response.body().data;
                                         friendshipRequests = EMPTY_FRIENDSHIP_REQUESTS;
                                         updateUserList();
                                     }
                                 }
                             }
                    );
        }
        return true;
    }

    private void updateUserList() {
        synchronized (sync) {
            if (users == null || friends == null || friendshipRequests == null) {
                return;
            }
        }
        boolean changed = !friendsInList.equals(friends) || !usersInList.equals(users) || !friendshipRequestsInList.equals(friendshipRequests);
        if (changed) {
            fastAdapter.clear();
            if (!friendshipRequests.isEmpty()) {
                fastAdapter.add(new HeaderItem("Friendship Requests"));
                for (Friendship f : friendshipRequests) {
                    fastAdapter.add(UserItem.create(f.relatedUser, fragNavController()));
                }
            }
            if (!friends.isEmpty()) {
                fastAdapter.add(new HeaderItem("Friends"));
                for (User u : friends) {
                    fastAdapter.add(UserItem.create(u, fragNavController()));
                }
            }
            if (!users.isEmpty()) {
                fastAdapter.add(new HeaderItem("Users"));
                for (User u : users) {
                    fastAdapter.add(UserItem.create(u, fragNavController()));
                }
            }
            friendshipRequestsInList = friendshipRequests;
            friendsInList = friends;
            usersInList = users;

        }
        synchronized (sync) {
            friends = null;
            users = null;
            friendshipRequests = null;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return onQueryTextChange(query);
    }

    public boolean onClick(View v, IAdapter<IItem> adapter, IItem item, int position) {
        if (item instanceof UserItem) {
            UserItem uItem = (UserItem) item;
            fragNavController().pushFragment(UserProfileFragment.newInstance(uItem.user.getUsername()));
            return true;
        }
        return false;
    }
}
