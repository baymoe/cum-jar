package moe.bay.cumjar;

import org.javacord.api.entity.permission.Role;

import java.util.Comparator;

public class RoleSizeComparator implements Comparator<Role> {
    @Override
    public int compare(Role r0, Role r1) {
        if (r0.getUsers().size()<r1.getUsers().size()) {
            return 1;
        } else {
            return -1;
        }
    }
}
