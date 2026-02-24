package com.cool.server.context;

public class BaseContext {
    
    private static final ThreadLocal<Long> currentId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();
    private static final ThreadLocal<Integer> currentRole = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        currentId.set(id);
    }

    public static Long getCurrentId() {
        return currentId.get();
    }

    public static void setCurrentUsername(String username) {
        currentUsername.set(username);
    }

    public static String getCurrentUsername() {
        return currentUsername.get();
    }

    public static void setCurrentRole(Integer role) {
        currentRole.set(role);
    }

    public static Integer getCurrentRole() {
        return currentRole.get();
    }

    public static void setCurrentUser(Long id, String username, Integer role) {
        currentId.set(id);
        currentUsername.set(username);
        currentRole.set(role);
    }

    public static void clear() {
        currentId.remove();
        currentUsername.remove();
        currentRole.remove();
    }

    public static void removeCurrentId() {
        currentId.remove();
    }
}
