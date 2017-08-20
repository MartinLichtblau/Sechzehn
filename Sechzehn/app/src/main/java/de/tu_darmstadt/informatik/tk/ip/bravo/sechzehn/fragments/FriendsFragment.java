package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.fragments;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.HeaderAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.io.IOException;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Friendship;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.LastChat;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Pagination;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.FragmentFriendsBinding;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.FriendshipItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.HeaderItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.LastChatItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items.UserItem;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.Asynchronous;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists.MapList;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists.PaginatedList;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists.ProxyList;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.ChatService.ChatService;
import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.FriendshipService.FriendshipService;
import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.UserService.UserService;
import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists.ConcatList.ConcatList;
import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists.HeaderList.HeaderList;
import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.lists.PaginatedList.PaginatedList;

/**
 * Provides the friendship requests, last chats and friends as well as the ability to search for friends and users.
 *
 * @author Alexander Geiß
 */
public class FriendsFragment extends DataBindingFragment<FragmentFriendsBinding> implements SearchView.OnQueryTextListener, FastAdapter.OnClickListener<IItem> {

    private static final HeaderItem HEADER_LAST_CHATS = new HeaderItem("Last Chats");
    private static final HeaderItem HEADER_FRIENDSHIP_REQUESTS = new HeaderItem("Friendship Requests");
    private static final HeaderItem HEADER_FRIENDS = new HeaderItem("Friends");
    private static final HeaderItem HEADER_USERS = new HeaderItem("Users");

    private static final int PER_PAGE = 10;

    private FastItemAdapter<IItem> fastAdapter = new FastItemAdapter<>();
    private boolean resume = false;
    private ProxyList<Friendship> friendshipRequests;
    private ProxyList<LastChat> lastChats;
    private ProxyList<User> friends;
    private PaginatedList<IItem> defaultList;
    private ProxyList<User> friendsSearch;
    private ProxyList<User> usersSearch;
    private PaginatedList<IItem> searchList;
    private boolean search = false;

    /**
     * OnScrollListener for endless scrolling.
     */
    private EndlessRecyclerOnScrollListener onScrollListener = new EndlessRecyclerOnScrollListener() {
        @Override
        public void onLoadMore(int currentPage) {
            if (search) {
                appendSearchListDataAsync(currentPage + 1, PER_PAGE);
            } else {
                appendDefaultListDataAsync(currentPage + 1, PER_PAGE);
            }
        }
    };
    /**
     * The current search query.
     */
    private String searchQuery;


    /**
     * Use this factory method to create a new object of
     * this fragment.
     *
     * @return A new instance of fragment FriendsFragment.
     */
    @NonNull
    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    /**
     * Initialises the FragmentFriendsBinding.
     *
     * @param inflater  The LayoutInflater object that can be used to inflate
     *                  any views in the fragment.
     * @param container This is the parent view that the fragment's
     *                  UI should be attached to.
     * @return Initialised FragmentFriendsBinding.
     */
    @Override
    protected FragmentFriendsBinding initDataBinding(LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFriendsBinding.inflate(inflater, container, false);
    }

    /**
     * Appends a data page to the search list.
     *
     * @param page    Number of the data page.
     * @param perPage Size of the data page.
     */
    @Asynchronous
    private void appendSearchListDataAsync(int page, int perPage) {
        new AsyncTask<Integer, Void, Pagination<IItem>>() {
            @Override
            protected Pagination<IItem> doInBackground(Integer... params) {
                Pagination<IItem> list = searchList.get(params[0], params[1]);
             ensureDataLoaded(list);
                return list;
            }

            @Override
            protected void onPostExecute(Pagination<IItem> iItemPagination) {
                fastAdapter.add(iItemPagination.data);
            }
        }.execute(page, perPage);
    }


    /**
     * Initialises the fastAdapter and onQueryTextListener.
     * If the fragment is not resumed it displays the default list,
     * else the content will be automatically created through the onQueryTextChangeEvent.
     *
     * @param binding The DataBinding of this Fragment.
     */
    @Override
    protected void useDataBinding(final FragmentFriendsBinding binding) {
        fastAdapter.withOnClickListener(this);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        binding.friendsList.setLayoutManager(llm);
        HeaderAdapter<HeaderItem> headerAdapter = new HeaderAdapter<>();
        binding.friendsList.setAdapter(
                headerAdapter.wrap(fastAdapter));
        if (!resume) {
            updateDefaultListAsync();
            resume = true;
        }
        binding.friendsSearch.setOnQueryTextListener(this);
    }

    /**
     * Updates the default list.
     * If the default list does not exists, the default list will be created by calling {@link #initDefaultList()}.
     */
    @Asynchronous
    public void updateDefaultListAsync() {
        new AsyncTask<Void, Void, Pagination<IItem>>() {
            @Override
            protected Pagination<IItem> doInBackground(Void... params) {
                if (defaultList == null) {
                    initDefaultList();
                } else {
                    friendshipRequests.reset();
                    lastChats.reset();
                    friends.reset();
                }

                Pagination<IItem> list=defaultList.get(1, PER_PAGE);
                ensureDataLoaded(list);
                return list;
            }

            @Override
            protected void onPostExecute(Pagination<IItem> iItemPagination) {
                search = false;
                fastAdapterReset(iItemPagination.data);
            }
        }.execute();
    }

    /**
     * Updates the search list to match the current search query.
     * If the search list does not exists, the search list will be created by calling {@link #initSearchList()}.
     */
    @Asynchronous
    private void updateSearchListAsync() {
        new AsyncTask<Void, Void, Pagination<IItem>>() {
            @Override
            protected Pagination<IItem> doInBackground(Void... params) {
                if (searchList == null) {
                    initSearchList();
                } else {
                    friendsSearch.reset();
                    usersSearch.reset();
                }
                Pagination<IItem> list = searchList.get(1, PER_PAGE);
                ensureDataLoaded(list);
                return list;
            }

            @Override
            protected void onPostExecute(Pagination<IItem> iItemPagination) {
                search = true;
                fastAdapterReset(iItemPagination.data);
            }
        }.execute();
    }

    /**
     * Updates, replaces, adds, removes the items in the fastAdapter.
     * During the update the onScrollListener will be disabled, afterwards it will be reset.
     *
     * @param items The items which will be present, after execution.
     */
    private void fastAdapterReset(@NonNull List<IItem> items) {
        binding.friendsList.removeOnScrollListener(onScrollListener);

        final int adapterItemCount = fastAdapter.getAdapterItemCount();
        final int size = items.size();
        int i = 0;
        for (; i < adapterItemCount && i < size; i++) {
            fastAdapter.set(i, items.get(i));
        }
        if (adapterItemCount > size) {
            fastAdapter.removeItemRange(i, adapterItemCount - i);
        } else if (adapterItemCount < size) {
            for (; i < size; i++) {
                fastAdapter.add(items.get(i));
            }
        }

        binding.friendsList.addOnScrollListener(onScrollListener);
        onScrollListener.resetPageCount(1);
    }

    /**
     * Initialises the default list.
     * Always call asynchronous.
     */
    private void initDefaultList() {
        friendshipRequests = new ProxyList<Friendship>() {
            @Override
            protected Pagination<Friendship> load(int page, int perPage) {
                try {
                    Response<Pagination<Friendship>> response = FriendshipService.getFriendshipRequests(SzUtils.getOwnername(), page, perPage).execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        lastChats = new ProxyList<LastChat>() {
            @Override
            protected Pagination<LastChat> load(int page, int perPage) {
                try {
                    Response<Pagination<LastChat>> response = ChatService.getLastChats(page, perPage).execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        friends = new ProxyList<User>() {
            @Override
            protected Pagination<User> load(int page, int perPage) {
                try {
                    Response<Pagination<User>> response = UserService.getFriends(SzUtils.getOwnername(), page, perPage).execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        defaultList = PaginatedList(ConcatList(
                HeaderList(HEADER_FRIENDSHIP_REQUESTS, new MapList<Friendship, IItem>(friendshipRequests) {
                    @Override
                    protected FriendshipItem map(Friendship item) {
                        return new FriendshipItem(item, FriendsFragment.this);
                    }
                }),
                HeaderList(HEADER_LAST_CHATS, new MapList<LastChat, IItem>(lastChats) {
                    @Override
                    protected LastChatItem map(LastChat item) {
                        return new LastChatItem(item);
                    }
                }),
                HeaderList(HEADER_FRIENDS, new MapList<User, IItem>(friends) {
                    @Override
                    protected UserItem map(User item) {
                        return new UserItem(item, FriendsFragment.this.fragNavController());
                    }
                })
        ));
    }

    /**
     * Initialises the search list.
     * Always call asynchronous.
     */
    private void initSearchList() {
        friendsSearch = new ProxyList<User>() {
            @Override
            protected Pagination<User> load(int page, int perPage) {
                try {
                    Response<Pagination<User>> response = UserService
                            .getUsers(page, perPage, null, null, null, true, searchQuery).execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        usersSearch = new ProxyList<User>() {
            @Override
            protected Pagination<User> load(int page, int perPage) {
                try {
                    Response<Pagination<User>> response = UserService
                            .getUsers(page, perPage, null, null, null, false, searchQuery).execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        searchList = PaginatedList(ConcatList(
                HeaderList(HEADER_FRIENDS, new MapList<User, IItem>(friendsSearch) {
                    @Override
                    protected UserItem map(User item) {
                        return new UserItem(item, FriendsFragment.this.fragNavController());
                    }
                }),
                HeaderList(HEADER_USERS, new MapList<User, IItem>(usersSearch) {
                    @Override
                    protected UserItem map(User item) {
                        return new UserItem(item, FriendsFragment.this.fragNavController());
                    }
                })
        ));
    }

    /**
     * Appends a data page to the default list.
     *
     * @param page    Number of the data page.
     * @param perPage Size of the data page.
     */
    @Asynchronous
    private void appendDefaultListDataAsync(int page, int perPage) {
        new AsyncTask<Integer, Void, Pagination<IItem>>() {
            @Override
            protected Pagination<IItem> doInBackground(Integer... params) {
                if (defaultList == null) return null;
                Pagination<IItem> list = defaultList.get(params[0], params[1]);
                ensureDataLoaded(list);
                return list;
            }

            @Override
            protected void onPostExecute(Pagination<IItem> iItemPagination) {
                if (iItemPagination == null) return;
                fastAdapter.add(iItemPagination.data);

            }
        }.execute(page, perPage);
    }

    /**
     * Ensures all data is loaded from the server.
     * @param page The list for which the data should be present.
     */
    private static void ensureDataLoaded(Pagination<IItem> page) {
        for (IItem i : page.data) {
            i.getType();
        }
    }

    /**
     * Called when the query text is changed by the user.
     * <p>
     * If there is no text, then the defaultList will be shown.
     *
     * @param newText The new query text.
     * @return True, everything is handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty()) {
            updateDefaultListAsync();
        } else {
            searchQuery = newText;
            updateSearchListAsync();
        }
        return true;
    }

    /**
     * Called when the user submits the query.
     * <p>
     * Calls {@link #onQueryTextChange(String) }.
     *
     * @param query the query text that is to be submitted.
     * @return true if the query has been handled by the listener, else false
     * @see #onQueryTextChange(String)
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return onQueryTextChange(query);
    }

    /**
     * Called if Item is clicked.
     * Handles the different item types and opens new fragments.
     *
     * @param v        View of clicked item.
     * @param adapter  Adapter of clicked item.
     * @param item     Item which is clicked.
     * @param position Position of clicked item.
     * @return True if element click is handled, else false.
     */
    @Override
    public boolean onClick(View v, IAdapter<IItem> adapter, IItem item, int position) {
        if (item instanceof UserItem) {
            UserItem uItem = (UserItem) item;
            fragNavController().pushFragment(UserProfileFragment.newInstance(uItem.getUser().getUsername()));
            return true;
        } else if (item instanceof FriendshipItem) {
            FriendshipItem fItem = (FriendshipItem) item;
            fragNavController().pushFragment(UserProfileFragment.newInstance(fItem.getFriendship().relatedUser.getUsername()));
            return true;
        } else if (item instanceof LastChatItem) {
            LastChatItem lItem = (LastChatItem) item;
            fragNavController().pushFragment(MessageFragment.newInstance(lItem.getUsername()));
            return true;
        }
        return false;
    }
}
