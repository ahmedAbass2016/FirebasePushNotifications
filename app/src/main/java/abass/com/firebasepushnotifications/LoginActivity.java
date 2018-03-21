package abass.com.firebasepushnotifications;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class  LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private Button mLoginBtn;
    private Button mRegPageBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private ProgressBar loginProgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = (EditText) findViewById(R.id.edemail);
        mPassword = (EditText) findViewById(R.id.edpassword);
        mLoginBtn = (Button) findViewById(R.id.btnLogin);
        mRegPageBtn = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        loginProgBar =(ProgressBar) findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mRegPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProgBar.setVisibility(View.VISIBLE);
                String email = mEmail.getText().toString();
                String Password = mPassword.getText().toString();
                if(email.equals("")){
                    loginProgBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this,"Error : You Must Enter Your Email",Toast.LENGTH_SHORT).show();
                }else if (Password.equals("")){
                    loginProgBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this,"Error :  You Must Enter Your Password",Toast.LENGTH_SHORT).show();
                }else{
                mAuth.signInWithEmailAndPassword(email,Password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information

                                    String token_Id = FirebaseInstanceId.getInstance().getToken();
                                    String current_Id = mAuth.getCurrentUser().getUid();
                                    Map<String, Object> tokenMap = new HashMap<>();
                                    tokenMap.put("token_id",token_Id);
                                    mFirestore.collection("Users").document(current_Id).update(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            SendToMain();
                                        }
                                    });

                                } else {
                                    loginProgBar.setVisibility(View.INVISIBLE);
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this,"Error : "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            }
        });


}

    private void SendToMain() {
        loginProgBar.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(LoginActivity.this, HelpRequest.class);
        startActivity(intent);
        finish();
    }


}