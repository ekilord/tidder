package com.nagyd.tidder.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseUser;
import com.nagyd.tidder.MainActivity;
import com.nagyd.tidder.R;
import com.nagyd.tidder.databinding.FragmentProfileBinding;
import com.nagyd.tidder.firebase.Auth;
import com.nagyd.tidder.firebase.Database;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        Database db = new Database();
        FirebaseUser currentUser = Auth.mAuth.getCurrentUser();

        EditText newUsernameField = binding.usernameChangeText;
        if (Objects.nonNull(currentUser)) {
            assert currentUser != null;
            db.getUserViaEmail(currentUser.getEmail()).thenAccept(user -> {
               newUsernameField.setHint(user.username);
            });
        }

        Button changeUsernameButton = binding.changeUsernameButton;
        changeUsernameButton.setOnClickListener(v -> {

            if (Objects.nonNull(currentUser)) {
                assert currentUser != null;
                db.getUserViaEmail(currentUser.getEmail()).thenAccept(user -> {
                    db.userExists(user.username, user.email).thenAccept(pair -> {
                        if (pair.first) {
                            String newUsername = binding.usernameChangeText.getText().toString();

                            db.updateUsername(user.username, newUsername).thenAccept(success -> {
                                if (success) {
                                    requireActivity().recreate();
                                    requireActivity().overridePendingTransition(0, 0);
                                }
                                else {
                                    Toast.makeText(requireActivity(), "Error while updating user in database", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        else {
                            Toast.makeText(requireActivity(), "Error while getting user from database", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });

        Button deleteProfileButton = binding.deleteProfileButton;
        deleteProfileButton.setOnClickListener(v -> {
            if (Objects.nonNull(currentUser)) {
                assert currentUser != null;
                db.getUserViaEmail(currentUser.getEmail()).thenAccept(user -> {
                    db.userExists(user.username, user.email).thenAccept(pair -> {
                        if (pair.first) {
                            db.deleteUser(user.username).thenAccept(success -> {
                                if (success) {
                                    currentUser.delete().addOnCompleteListener(task -> {
                                        requireActivity().recreate();
                                        requireActivity().overridePendingTransition(0, 0);

                                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                                        navController.navigate(R.id.nav_feed);
                                    });
                                }
                                else {
                                    Toast.makeText(requireActivity(), "Error while deleting user from database", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            Toast.makeText(requireActivity(), "Error while getting user from database", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}