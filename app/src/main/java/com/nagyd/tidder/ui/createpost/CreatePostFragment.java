package com.nagyd.tidder.ui.createpost;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.nagyd.tidder.R;
import com.nagyd.tidder.databinding.FragmentCreatePostBinding;
import com.nagyd.tidder.firebase.Auth;
import com.nagyd.tidder.firebase.Database;
import com.nagyd.tidder.model.User;
import com.nagyd.tidder.ui.sub.SubFragment;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CreatePostFragment extends Fragment {
    private FragmentCreatePostBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);

        Button createSubButton = binding.createPostButton;

        createSubButton.setOnClickListener(v -> {
            Database db = new Database();

            CompletableFuture<User> getUserFuture =  db.getUserViaEmail(Objects.requireNonNull(Auth.mAuth.getCurrentUser()).getEmail());

            getUserFuture.thenAccept(got -> {
                if (Objects.nonNull(got)) {
                    String title = binding.createPostTitle.getText().toString();
                    String author = got.username;
                    String desc = binding.createPostDesc.getText().toString();
                    String id = (System.currentTimeMillis() / 1000L) + "_" + got.username;

                    CompletableFuture<Boolean> uploadPostFuture =  db.uploadPost(title, author, desc, id, SubFragment.name);

                    uploadPostFuture.thenAccept(success -> {
                        if (success) {
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                            navController.popBackStack();
                        }
                        else {
                            Toast.makeText(this.getContext(), "Error uploading post!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(this.getContext(), "Error getting logged in user!", Toast.LENGTH_SHORT).show();
                }
            });



        });

    return binding.getRoot();
    }
}