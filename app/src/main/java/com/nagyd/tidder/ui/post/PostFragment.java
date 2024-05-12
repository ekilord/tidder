package com.nagyd.tidder.ui.post;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nagyd.tidder.R;
import com.nagyd.tidder.databinding.FragmentPostBinding;
import com.nagyd.tidder.firebase.Auth;
import com.nagyd.tidder.firebase.Database;
import com.nagyd.tidder.model.Comment;
import com.nagyd.tidder.model.Post;
import com.nagyd.tidder.model.User;
import com.nagyd.tidder.ui.sub.SubFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PostFragment extends Fragment {
    public static Post currentPost;
    private FragmentPostBinding binding;

    private SubAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostBinding.inflate(inflater, container, false);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(currentPost.title.substring(0, Math.min(currentPost.title.length(), 20)));

        binding.postTitle.setText(PostFragment.currentPost.title);
        binding.postAuthor.setText(PostFragment.currentPost.author);
        binding.postText.setText(PostFragment.currentPost.desc);

        RecyclerView recyclerView = binding.recviewCommentList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        Database db = new Database();

        db.getComments(SubFragment.name, PostFragment.currentPost.id).thenAccept(comments -> {
            adapter.updateComments(comments);
        });

        Button postButton = binding.postCommentButton;
        TextView commentInputText = binding.postCommentText;

        if (Objects.nonNull(Auth.mAuth.getCurrentUser())) {
            postButton.setOnClickListener(v -> {
                CompletableFuture<User> getUserFuture =  db.getUserViaEmail(Objects.requireNonNull(Auth.mAuth.getCurrentUser()).getEmail());

                getUserFuture.thenAccept(got -> {
                    if (Objects.nonNull(got)) {
                        String text = binding.postCommentText.getText().toString();
                        String author = got.username;
                        String id = (System.currentTimeMillis() / 1000L) + "_" + got.username;

                        CompletableFuture<Boolean> uploadPostFuture =  db.uploadComment(text, author, id, SubFragment.name, PostFragment.currentPost.id);

                        uploadPostFuture.thenAccept(success -> {
                            if (success) {
                                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                                navController.popBackStack();
                                navController.navigate(R.id.nav_post);
                            }
                            else {
                                Toast.makeText(this.getContext(), "Error uploading comment!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        Toast.makeText(this.getContext(), "Error getting logged in user!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        else {
            postButton.setVisibility(View.GONE);
            commentInputText.setVisibility(View.GONE);
        }


        return binding.getRoot();
    }

    private class SubAdapter extends RecyclerView.Adapter<SubViewHolder> {

        private final List<Comment> comments;

        public SubAdapter(List<Comment> comments) {
            this.comments = comments;
        }

        public void updateComments(List<Comment> newComments) {
            comments.clear();
            comments.addAll(newComments);
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
            Comment comment = comments.get(position);
            holder.titleTextView.setText(comment.author);
            holder.descriptionTextView.setText(comment.text);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }
    }

    private class SubViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView descriptionTextView;

        public SubViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textPostName);
            descriptionTextView = itemView.findViewById(R.id.textPostDesc);
        }
    }
}