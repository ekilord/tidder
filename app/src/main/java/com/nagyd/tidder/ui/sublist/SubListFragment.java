package com.nagyd.tidder.ui.sublist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nagyd.tidder.R;
import com.nagyd.tidder.databinding.FragmentSubListBinding;
import com.nagyd.tidder.firebase.Auth;
import com.nagyd.tidder.model.Sub;
import com.nagyd.tidder.ui.sub.SubFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.nagyd.tidder.firebase.Database;

public class SubListFragment extends Fragment {
    private FragmentSubListBinding binding;
    private SubAdapter adapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSubListBinding.inflate(inflater, container, false);

        RecyclerView recyclerView = binding.recviewSublist;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        FloatingActionButton createSubFloatingButton = binding.createSubFloatingButton;

        if (Objects.nonNull(Auth.mAuth.getCurrentUser())) {
            createSubFloatingButton.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_createsub);
            });
        }
        else {
            createSubFloatingButton.setVisibility(View.GONE);
        }

        Database db = new Database();

        db.getSubs().thenAccept(subs -> {
            adapter.updateSubs(subs);
        });

        return binding.getRoot();
    }

    private class SubAdapter extends RecyclerView.Adapter<SubViewHolder> {

        private final List<Sub> subs;

        public SubAdapter(List<Sub> subs) {
            this.subs = subs;
        }

        public void updateSubs(List<Sub> newSubs) {
            subs.clear();
            subs.addAll(newSubs);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SubViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.recview_sublist_row, parent, false);
            return new SubViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SubViewHolder holder, int position) {
            Sub sub = subs.get(position);
            holder.nameTextView.setText(sub.name);
            holder.descriptionTextView.setText(sub.desc);

            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
            holder.constraintLayout.startAnimation(animation);
        }

        @Override
        public int getItemCount() {
            return subs.size();
        }
    }

    private class SubViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final TextView descriptionTextView;
        private final ConstraintLayout constraintLayout;

        public SubViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textSubName);
            descriptionTextView = itemView.findViewById(R.id.textSubDesc);
            constraintLayout = itemView.findViewById(R.id.layoutSubList);
            itemView.setOnClickListener((View view) -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Sub sub = adapter.subs.get(position);

                        SubFragment.name = sub.name;

                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                        navController.navigate(R.id.nav_sub);
                    }
            });
        }
    }
}