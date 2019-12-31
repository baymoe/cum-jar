package moe.bay.cumjar;

import org.javacord.api.entity.user.User;

public class CumUser {

    private final User user;

    public CumUser(final User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    public boolean isAdmin() {
        return App.botAdminIds.contains(this.getUser().getId());
    }
}