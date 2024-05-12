package com.nagyd.tidder.firebase;


import android.util.Log;
import android.util.Pair;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nagyd.tidder.model.Comment;
import com.nagyd.tidder.model.Post;
import com.nagyd.tidder.model.Sub;
import com.nagyd.tidder.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class Database {
    private final FirebaseFirestore mDb;

    public Database() {
        this.mDb = FirebaseFirestore.getInstance();
    }

    public CompletableFuture<Pair<Boolean, Boolean>> userExists(String username, String email) {
        CompletableFuture<Pair<Boolean, Boolean>> future = new CompletableFuture<>();

        CollectionReference users = this.mDb.collection("users");

        Query usernameExistsQuery = users.whereEqualTo("username", username);
        usernameExistsQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    Pair<Boolean, Boolean> exists = Pair.create(true, false);
                    future.complete(exists);
                }
                else {
                    Query useremailExistsQuery = users.whereEqualTo("email", email);
                    useremailExistsQuery.get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            if (!task2.getResult().isEmpty()) {
                                Pair<Boolean, Boolean> exists = Pair.create(false, true);
                                future.complete(exists);
                            }
                            else {
                                Pair<Boolean, Boolean> exists = Pair.create(false, false);
                                future.complete(exists);
                            }
                        }
                    }).addOnFailureListener(future::completeExceptionally);
                }
            }
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<User> getUserViaEmail(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();

        this.mDb.collection("users").get().addOnSuccessListener(docs -> {

            for (QueryDocumentSnapshot doc : docs) {
                User user = doc.toObject(User.class);
                if (Objects.equals(email, user.email)) {
                    future.complete(user);
                    return;
                }

            }
            future.complete(null);
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Boolean> uploadUser(String username, String email) {
        User user = new User(username, email);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        mDb.collection("users").document().set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                future.complete(true);
            }
            else future.complete(false);
        });

        return future;
    }

    public CompletableFuture<Boolean> subExists(String name) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        this.mDb.collection("subs").document(name).get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                future.complete(true);
            }
            else future.complete(false);
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Boolean> uploadSub(String name, String desc) {
        Sub sub = new Sub(name, desc);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        mDb.collection("subs").document(sub.name).set(sub).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                future.complete(true);
            }
            else future.complete(false);
        });

        return future;
    }

    public CompletableFuture<Boolean> uploadPost(String name, String author, String desc, String id, String parent) {
        Post post = new Post(name, author, desc, parent, id);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        mDb.collection("subs").document(parent).collection("posts").document(id).set(post).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                future.complete(true);
            }
            else future.complete(false);
        });

        return future;
    }

    public CompletableFuture<Boolean> uploadComment(String text, String author, String id, String subName, String postId) {
        Comment comment = new Comment(text, author, id);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        mDb.collection("subs").document(subName).collection("posts").document(postId).collection("comments").document().set(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                future.complete(true);
            }
            else future.complete(false);
        });

        return future;
    }

    public CompletableFuture<List<Sub>> getSubs() {
        CompletableFuture<List<Sub>> future = new CompletableFuture<>();
        List<Sub> subs = new ArrayList<>();

        this.mDb.collection("subs").get().addOnSuccessListener(docs -> {
            for (QueryDocumentSnapshot doc : docs) {
                subs.add(doc.toObject(Sub.class));
            }
            future.complete(subs);
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<List<Post>> getPosts(String sub) {
        CompletableFuture<List<Post>> future = new CompletableFuture<>();
        List<Post> posts = new ArrayList<>();

        this.mDb.collection("subs").document(sub).collection("posts").get().addOnSuccessListener(docs -> {
            for (QueryDocumentSnapshot doc : docs) {
                posts.add(doc.toObject(Post.class));
            }
            future.complete(posts);
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<List<Comment>> getComments(String subName, String postId) {
        CompletableFuture<List<Comment>> future = new CompletableFuture<>();
        List<Comment> comments = new ArrayList<>();

        mDb.collection("subs").document(subName).collection("posts").document(postId).collection("comments").get().addOnSuccessListener(docs -> {
            for (QueryDocumentSnapshot doc : docs) {
                comments.add(doc.toObject(Comment.class));
            }
            future.complete(comments);
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<List<Post>> getTopPosts() {
        CompletableFuture<List<Post>> future = new CompletableFuture<>();
        List<List<Post>> allSubPosts = new ArrayList<>();

        this.getSubs().thenAccept(subs -> {
            for (Sub sub : subs) {
                CollectionReference subPosts = this.mDb.collection("subs").document(sub.name).collection("posts");
                Query topPostsQuery = subPosts.orderBy("id", Query.Direction.DESCENDING).limit(10);
                topPostsQuery.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Post> subForumPosts = task.getResult().toObjects(Post.class);
                        allSubPosts.add(subForumPosts);
                    }
                    if (allSubPosts.size() == subs.size()) {
                        List<Post> finalList = allSubPosts.stream().flatMap(Collection::stream).collect(Collectors.toList());
                        future.complete(finalList);
                    }
                });
            }
        });
        return future;
    }

    public CompletableFuture<Boolean> updateUsername(String username, String newUsername) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        CollectionReference usersRef = mDb.collection("users");
        Query userQuery = usersRef.whereEqualTo("username", username);

        userQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    String userId = document.getId();
                    DocumentReference userRef = mDb.collection("users").document(userId);

                    userRef.update("username", newUsername)
                            .addOnSuccessListener(unused -> future.complete(true))
                            .addOnFailureListener(future::completeExceptionally);
                }
            }
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Boolean> deleteUser(String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        CollectionReference usersRef = mDb.collection("users");
        Query userQuery = usersRef.whereEqualTo("username", username);

        userQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    String userId = document.getId();
                    DocumentReference userRef = mDb.collection("users").document(userId);

                    userRef.delete().addOnSuccessListener(task2 -> future.complete(true))
                            .addOnFailureListener(future::completeExceptionally);
                }
            }
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }
}
