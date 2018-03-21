package abass.com.firebasepushnotifications;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HelpRequest extends AppCompatActivity {

    private Spinner spinner;
    private EditText requestText;
    private Button SendRequestBtn;
    private String Message;
    private String Domain;

    private double longtitude, latitude;

    private String mCurrentID;
    private String mCurrentName;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mfirestore;

    private FusedLocationProviderClient client;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser CurrentUser = mAuth.getCurrentUser();
        if(CurrentUser == null ){
            sendToLogin();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_request);

        /*  Start Spinner Code */
        spinner = (Spinner) findViewById(R.id.planets_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        /*  End Spinner Code */

        requestText = (EditText) findViewById(R.id.text_help);
        SendRequestBtn=(Button) findViewById(R.id.sendrequest);

        client = LocationServices.getFusedLocationProviderClient(HelpRequest.this);

        requestPermission();

        mAuth = FirebaseAuth.getInstance();

        SendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mfirestore = FirebaseFirestore.getInstance();
                mCurrentID = mAuth.getUid();

                mfirestore.collection("Users").document(mCurrentID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        mCurrentName = documentSnapshot.get("name").toString();
                    }
                });

                Message = requestText.getText().toString();
                Domain = spinner.getSelectedItem().toString();

                if (ActivityCompat.checkSelfPermission( HelpRequest.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                client.getLastLocation().addOnSuccessListener( HelpRequest.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null){
                            longtitude = location.getLongitude();
                            latitude = location.getLatitude();
                        }
                    }
                });
                mfirestore.collection("Users").addSnapshotListener(HelpRequest.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                            if(doc.getType()== DocumentChange.Type.ADDED){
                                String user_id = doc.getDocument().getId();

                                if(user_id.equals(mCurrentID)){
                                    continue;
                                }

                                Map<String , Object> notificationMessage = new HashMap<>();
                                notificationMessage.put("message", Message);
                                notificationMessage.put("from", mCurrentID);
                                notificationMessage.put("user_name", mCurrentName);
                                notificationMessage.put("domain", Domain);
                                notificationMessage.put("longtitude",longtitude);
                                notificationMessage.put("latitude",latitude);

                                mfirestore.collection("Users/"+user_id+"/Notifications").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(HelpRequest.this,"Error :  "+ e.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                        }
                        Toast.makeText(HelpRequest.this,"The Help Request Sent ",Toast.LENGTH_LONG).show();
                    }
                });

            }
        });


    }
    private void sendToLogin() {
        Intent intent = new Intent(HelpRequest.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(HelpRequest.this,new String[]{ACCESS_FINE_LOCATION},1);
    }
}
