package abass.com.firebasepushnotifications.Request;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import abass.com.firebasepushnotifications.R;

/**
 * Created by ahmed on 27-Mar-18.
 */

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.ViewHolder> {

    FirebaseFirestore db;
    private List<MyNotification> notificationsList;
    private Context context;


    public NotificationsRecyclerAdapter(Context context, List<MyNotification> notificationsList) {
        this.notificationsList = notificationsList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.user_name_view.setText(notificationsList.get(position).getUser_name());
        holder.Domain_view.setText(notificationsList.get(position).getDomain());

        final String Notification_Id = notificationsList.get(position).notificationId;
        String Current_User_Id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("Users").document(Current_User_Id).collection("Notifications").document(Notification_Id);

        holder.mview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Loading");
                progressDialog.setMessage("Please Wait until Notification Loads");
                progressDialog.show();
                    if (isNetworkAvailable()) {
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                MyNotification notification = documentSnapshot.toObject(MyNotification.class);
                                GoToNotifications(notification);
                                progressDialog.hide();
                            }
                        });
                    } else {
                        progressDialog.hide();
                        Toast.makeText(context, "Check Your Internet Connection.", Toast.LENGTH_SHORT).show();
                    }
            }
        });


    }

    private void GoToNotifications(MyNotification notification) {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra("message", notification.getMessage());
        intent.putExtra("from_user_id", notification.getUser_name());
        intent.putExtra("latitude", notification.getLatitude());
        intent.putExtra("longtitude", notification.getLongtitude());
        intent.putExtra("domain", notification.getDomain());
        intent.putExtra("request_id", notification.getRequestID());
        intent.putExtra("type", notification.getType());
        context.startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mview;

        private TextView user_name_view, Domain_view;

        public ViewHolder(View itemView) {
            super(itemView);

            mview = itemView;

            user_name_view = (TextView) mview.findViewById(R.id.sender_name);
            Domain_view = (TextView) mview.findViewById(R.id.domain);

        }
    }


}
