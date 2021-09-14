package com.example.forum.constant;

public class Authority {
    public static final String[] USER_AUTHORITIES = {"read", "createPost", "createTopic", "postComment", "deleteOwnPost", "deleteOwnComment"};
    public static final String[] ADMIN_AUTHORITIES = {"read", "createPost", "createTopic", "postComment", "deleteAnyPost", "deleteOwnComment", "addUser", "deleteUser", "editUser"};
}
