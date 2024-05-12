package com.nagyd.tidder.ui.createsub;

import android.os.Bundle;
import android.util.Log;
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
import com.nagyd.tidder.databinding.FragmentCreateSubBinding;

import com.nagyd.tidder.firebase.Database;
import com.nagyd.tidder.ui.sub.SubFragment;

import java.util.concurrent.CompletableFuture;

public class CreateSubFragment extends Fragment {
    private FragmentCreateSubBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateSubBinding.inflate(inflater, container, false);

        Button createSubButton = binding.createSubButton;

        createSubButton.setOnClickListener(v -> {
            String name = binding.createSubName.getText().toString();
            String desc = binding.createSubDesc.getText().toString();

            if (name.length() >= 3) {
                if (desc.length() > 8) {
                    Database db = new Database();
                    CompletableFuture<Boolean> subExistsFuture = db.subExists(name);

                    subExistsFuture.thenAccept(exists -> {
                        if (exists) {
                            Toast.makeText(this.getContext(), "A sub with this name has been already made!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            CompletableFuture<Boolean> subUploadFuture = db.uploadSub(name, desc);

                            subUploadFuture.thenAccept(done -> {
                                if (done) {
                                    SubFragment.name = name;

                                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                                    navController.navigate(R.id.nav_feed);
                                }
                                else {
                                    Toast.makeText(this.getContext(), "Error with creating new sub!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else {
                    Toast.makeText(this.getContext(), "The description of the sub should be at least 8 characters long!", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this.getContext(), "The name of the sub should be at least 3 characters long!", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }
}