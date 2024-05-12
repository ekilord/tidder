package com.nagyd.tidder.ui.login;

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

import com.nagyd.tidder.firebase.Auth;
import com.nagyd.tidder.R;
import com.nagyd.tidder.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        Button loginButton = binding.loginButton;
        loginButton.setOnClickListener(v -> Auth.mAuth.signInWithEmailAndPassword(binding.loginEmail.getText().toString(), binding.loginPassword.getText().toString()).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                onLoginSuccess();
            } else {
                onLoginFailure();
            }
        }));

        Button signUpButton = binding.redirectToSignUpButton;
        signUpButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_signup);
        });

        return binding.getRoot();
    }

    private void onLoginSuccess() {
        refreshMainActivity();

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_feed);
    }

    private void onLoginFailure() {
        Toast.makeText(requireActivity(), "Incorrect credentials", Toast.LENGTH_SHORT).show();
    }

    private void refreshMainActivity() {
        requireActivity().recreate();
        requireActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}