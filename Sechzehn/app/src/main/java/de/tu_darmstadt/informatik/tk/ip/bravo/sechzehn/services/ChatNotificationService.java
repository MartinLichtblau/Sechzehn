package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;

import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.R;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.activities.BottomTabsActivity;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.ChatUser;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.Message;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.data.User;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.socket.ChatSocket;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.IgnoreErrorCallback;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.IgnoreErrorTarget;
import de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils.SzUtils;
import retrofit2.Call;
import retrofit2.Response;

import static de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.network.services.UserService.UserService;

/**
 * Android Service which provides the Notifications for chat messages
 *
 * @author Alexander Geiß on 10.08.2017.
 */

public class ChatNotificationService extends Service {


    private final static int MESSAGE_NOTIFICATION_OFFSET = (1 << 16);


    private ChatSocket socket;

    private ChatNotificationService self = this;

    private static HashSet<String> blockedUsers = new HashSet<>();

    private NotificationManager notificationManager;

    private final ChatSocket.Listener messageListener = new ChatSocket.Listener() {
        @Override
        public void call(final Message msg) {
            if (msg.receiver.equals(SzUtils.getOwnername())
                    && !blockedUsers.contains(msg.sender)) {
                UserService.getUser(msg.sender).enqueue(new IgnoreErrorCallback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            ChatNotificationService.this.notify(response.body(), msg);
                        }
                    }
                });

            }
        }
    };

    public static void blockUser(String username) {
        blockedUsers.add(username);
    }

    public static void unblockUser(String username) {
        if (blockedUsers.contains(username)) {
            blockedUsers.remove(username);
        }
    }

    public void notify(User user, final Message msg) {
        msg.senderUser = new ChatUser(user);
        Picasso.with(self)
                .load(user.getProfilePicture())
                .resize(40, 40)
                .centerCrop()
                .transform(SzUtils.CROP_CIRCLE_TRANSFORMATION)
                .into(new IgnoreErrorTarget() {
                    @Override
                    public void onBitmapLoaded(Bitmap largeIcon, Picasso.LoadedFrom from) {
                        createContentIntent(msg);
                        Notification notification = new NotificationCompat.Builder(self)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(msg.senderUser.getUser().getOptionalRealName())
                                .setContentText(msg.body)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setCategory(Notification.CATEGORY_MESSAGE)
                                .setColor(getResources().getColor(R.color.accent, null))
                                .setLargeIcon(largeIcon)
                                .setContentIntent(createContentIntent(msg))
                                .build();

                        notificationManager.notify(getNotificationId(msg), notification);
                    }
                });
    }

    public PendingIntent createContentIntent(Message msg) {
        Intent intent = new Intent(self, BottomTabsActivity.class)
                .putExtra(Intent.EXTRA_INTENT, BottomTabsActivity.EXTRA_INTENT_SHOW_MESSAGE)
                .putExtra(Intent.EXTRA_USER, msg.sender);

        return PendingIntent.getActivity(self, 0, intent, 0);
    }

    /**
     * Calculates the notification ID based on {@link Message#id} and {@link #MESSAGE_NOTIFICATION_OFFSET}
     *
     * @param msg The message for which the id is calculated.
     * @return The calculated notification ID.
     */
    public static int getNotificationId(Message msg) {
        return msg.id + MESSAGE_NOTIFICATION_OFFSET;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (socket == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            socket = new ChatSocket();
            socket.addOnMessageListener(messageListener);
        }

        socket.connect();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        socket.disconnect();
        socket = null;
    }

    /**
     * The Service is not bindable.
     *
     * @param intent Intent
     * @return Always null.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
