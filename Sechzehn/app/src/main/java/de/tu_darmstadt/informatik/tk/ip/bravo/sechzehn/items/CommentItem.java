package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.view.View;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.NestedListView;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.databinding.ItemCommentBinding;

/**
 * @author Alexander Geiß on 21.08.2017.
 */

public class CommentItem extends NestedListView.ItemDB<ItemCommentBinding> {
    @Override
    public int getLayoutRes() {
        return R.layout.item_comment;
    }

    @Override
    protected ItemCommentBinding initDataBinding(View view) {
        return ItemCommentBinding.bind(view);
    }
}
