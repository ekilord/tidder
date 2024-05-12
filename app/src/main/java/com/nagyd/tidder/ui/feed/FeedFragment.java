package com.nagyd.tidder.ui.feed;

import android.os.Bundle;
import android.util.Log;
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
import com.nagyd.tidder.databinding.FragmentFeedBinding;
import com.nagyd.tidder.firebase.Database;
import com.nagyd.tidder.model.Post;
import com.nagyd.tidder.ui.post.PostFragment;
import com.nagyd.tidder.ui.sub.SubFragment;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;

    private SubAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFeedBinding.inflate(inflater, container, false);

        RecyclerView recyclerView = binding.recviewTopPostList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        Database db = new Database();

        db.getTopPosts().thenAccept(posts -> {
            adapter.updateSubs(posts);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class SubAdapter extends RecyclerView.Adapter<SubViewHolder> {

        private final List<Post> posts;

        public SubAdapter(List<Post> posts) {
            this.posts = posts;
        }

        public void updateSubs(List<Post> newPosts) {
            posts.clear();
            posts.addAll(newPosts);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SubViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.recview_topposts_row, parent, false);
            return new SubViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SubViewHolder holder, int position) {
            Post post = posts.get(position);
            holder.titleTextView.setText(post.title);
            holder.descriptionTextView.setText(post.desc);
            holder.subNameTextView.setText(post.parent);

            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_from_left);
            holder.constraintLayout.startAnimation(animation);
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }
    }

    private class SubViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView subNameTextView;
        private final ConstraintLayout constraintLayout;

        public SubViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textPostName);
            descriptionTextView = itemView.findViewById(R.id.textPostDesc);
            subNameTextView = itemView.findViewById(R.id.textPostSubName);
            constraintLayout = itemView.findViewById(R.id.layoutTopPosts);
            itemView.setOnClickListener((View view) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Post post = adapter.posts.get(position);

                    SubFragment.name = post.parent;
                    PostFragment.currentPost = post;

                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.nav_post);
                }
            });
        }
    }
}