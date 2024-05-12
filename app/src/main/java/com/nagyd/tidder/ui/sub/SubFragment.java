package com.nagyd.tidder.ui.sub;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nagyd.tidder.R;
import com.nagyd.tidder.databinding.FragmentSubBinding;
import com.nagyd.tidder.firebase.Auth;
import com.nagyd.tidder.firebase.Database;
import com.nagyd.tidder.model.Post;
import com.nagyd.tidder.ui.post.PostFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SubFragment extends Fragment {
    public static String name;

    private FragmentSubBinding binding;

    private SubAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSubBinding.inflate(inflater, container, false);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(name);

        RecyclerView recyclerView = binding.recviewPostlist;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        FloatingActionButton createPostFloatingButtonButton = binding.createPostFloatingButton;

        if (Objects.nonNull(Auth.mAuth.getCurrentUser())) {
            createPostFloatingButtonButton.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_createpost);
            });
        }
        else {
            createPostFloatingButtonButton.setVisibility(View.GONE);
        }


        Database db = new Database();

        db.getPosts(SubFragment.name).thenAccept(posts -> {
            adapter.updatePosts(posts);
        });

        return binding.getRoot();
    }

    private class SubAdapter extends RecyclerView.Adapter<SubViewHolder> {

        private List<Post> posts;

        public SubAdapter(List<Post> posts) {
            this.posts = posts;
        }

        public void updatePosts(List<Post> newPosts) {
            posts.clear();
            posts.addAll(newPosts);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SubViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.recview_postlist_row, parent, false);
            return new SubViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SubViewHolder holder, int position) {
            Post post = posts.get(position);
            holder.titleTextView.setText(post.title);
            holder.descriptionTextView.setText(post.desc);
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }
    }

    private class SubViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView descriptionTextView;

        public SubViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textPostName);
            descriptionTextView = itemView.findViewById(R.id.textPostDesc);
            itemView.setOnClickListener((View view) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {

                    PostFragment.currentPost = adapter.posts.get(position);

                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.nav_post);
                }
            });
        }
    }
}