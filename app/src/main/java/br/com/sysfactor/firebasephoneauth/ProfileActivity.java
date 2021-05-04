package br.com.sysfactor.firebasephoneauth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.sysfactor.firebasephoneauth.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "PROFILE_ACTIVITY_TAG";

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        checkUserStatus();

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                checkUserStatus();
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = auth.getCurrentUser();
        if(user != null) {
            phoneNumber = user.getPhoneNumber();
            binding.txtPhoneNumber.setText(phoneNumber);
        } else {
            finish();
        }
    }
}