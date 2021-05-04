package br.com.sysfactor.firebasephoneauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import br.com.sysfactor.firebasephoneauth.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verification;
    private String phoneNumber;

    private static final String TAG = "MAIN_ACTIVITY_TAG";

    private FirebaseAuth auth;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        binding.phoneLayout.setVisibility(View.VISIBLE);
        binding.codeLayout.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Por favor, espere...");
        dialog.setCanceledOnTouchOutside(false);

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInUser(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(s, token);
                Log.d(TAG, "onCodeSent:"+ s);

                verification = s;
                resendingToken = token;
                dialog.dismiss();


                binding.codeLayout.setVisibility(View.VISIBLE);
                binding.phoneLayout.setVisibility(View.GONE);

                Toast.makeText(MainActivity.this, "Código de verificação enviado...", Toast.LENGTH_SHORT).show();
                phoneNumber = binding.phoneEt.getText().toString().trim();
                binding.codeSentDescription.setText("Por favor, entre o código enviado \npara "+phoneNumber);
            }
        };

        binding.phoneContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = binding.phoneEt.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(MainActivity.this, "Por favor, informe um número de telefone.", Toast.LENGTH_SHORT).show();
                } else {
                    verifyPhoneNumber(phoneNumber);
                }

            }
        });

        binding.txtResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = binding.phoneEt.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(MainActivity.this, "Por favor, informe um número de telefone.", Toast.LENGTH_SHORT).show();
                } else {
                    resendVerificationCode(phoneNumber, resendingToken);
                }
            }
        });

        binding.codeSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = binding.codeEt.getText().toString().trim();
                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(MainActivity.this, "Por favor, informe o código de verificação.", Toast.LENGTH_SHORT).show();
                } else {
                    verifyPhoneNumberWithCode(verification, code);
                }
            }
        });


    }


    private void verifyPhoneNumber(String phoneNumber) {
        dialog.setMessage("Verificando " + phoneNumber);
        dialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        dialog.setMessage("Reenviando código para " + phoneNumber);
        dialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setForceResendingToken(token)
                .setCallbacks(callbacks).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String verification, String code) {
        dialog.setMessage("Verificando código recebido...");
        dialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification, code);
        signInUser(credential);
    }

    private void signInUser(PhoneAuthCredential credential) {
        dialog.setMessage("Fazendo Login...");
        auth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        dialog.dismiss();
                        String phoneNumber = auth.getCurrentUser().getPhoneNumber();
                        Toast.makeText(MainActivity.this, "Logado como: "+phoneNumber, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}