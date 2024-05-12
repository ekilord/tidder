package com.nagyd.tidder.firebase;

import com.google.firebase.auth.FirebaseAuth;

public class Auth {
    public static FirebaseAuth mAuth;

    public static void init() {
        mAuth = FirebaseAuth.getInstance();
    }
}