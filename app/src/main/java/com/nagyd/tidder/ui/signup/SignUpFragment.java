package com.nagyd.tidder.ui.signup;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.nagyd.tidder.MainActivity;
import com.nagyd.tidder.R;
import com.nagyd.tidder.databinding.FragmentSignupBinding;
import com.nagyd.tidder.ui.post.PostFragment;
import com.nagyd.tidder.utils.TextUtils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.nagyd.tidder.firebase.Auth;
import com.nagyd.tidder.firebase.Database;

public class SignUpFragment extends Fragment {
    private FragmentSignupBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);

        Button signUpButton = binding.signUpButton;
        signUpButton.setOnClickListener(v -> {
            String username = binding.signUpName.getText().toString();
            String email = binding.signUpEmail.getText().toString();
            String password = binding.signUpPassword.getText().toString();
            String confirmPassword = binding.signUpConfirmPassword.getText().toString();

            if (username != null && username.length() > 3) {
                if (email != null && TextUtils.isEmail(email)) {
                    if (password != null && password.length() > 6) {
                        if (password.equals(confirmPassword)) {
                            Database db = new Database();
                            CompletableFuture<Pair<Boolean, Boolean>> userExistsFuture = db.userExists(username, email);

                            userExistsFuture.thenAccept(exists -> {
                                if (exists.first) {
                                    Toast.makeText(this.getContext(), "A user exists already with this name", Toast.LENGTH_SHORT).show();
                                }
                                else if (exists.second) {
                                    Toast.makeText(this.getContext(), "A user exists already with this email", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Auth.mAuth.createUserWithEmailAndPassword(binding.signUpEmail.getText().toString(), binding.signUpPassword.getText().toString()).addOnCompleteListener(requireActivity(), task -> {
                                        if (task.isSuccessful()) {
                                            db.uploadUser(username, email);
                                            onSignUpSuccess();
                                        } else {
                                            onSignUpFailure(task);
                                        }
                                    });
                                }
                            });
                        }
                        else {
                            Toast.makeText(this.getContext(), "The passwords do not match", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(this.getContext(), "The password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(this.getContext(), "The format of the email is incorrect", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this.getContext(), "The username must be at least 3 characters long", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onSignUpSuccess() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_feed);

        requireActivity().recreate();
        requireActivity().overridePendingTransition(0, 0);
    }

    private void onSignUpFailure(Task<AuthResult> task) {
        Toast.makeText(this.getContext(), "Error while registering user:\n" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
    }

}