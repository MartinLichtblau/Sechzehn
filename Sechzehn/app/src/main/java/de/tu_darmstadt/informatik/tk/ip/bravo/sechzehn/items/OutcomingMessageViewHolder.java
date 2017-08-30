package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.items;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Message;

/**
 * Add support for reading reciepts to outgoing messages.
 * @author Alexander Geiß on 10.08.2017.
 */

public class OutcomingMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<Message> {

    public OutcomingMessageViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        if (message.isRead) {
            time.setText(time.getText()+" ✔");
        }

    }
}