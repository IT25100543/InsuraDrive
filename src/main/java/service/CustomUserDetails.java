package com.insuradrive.service;

import com.insuradrive.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;
    private final String roleName;
    private final String username;
    private final String password;
    private final String displayName;
    private final String accountStatus;

    public CustomUserDetails(User user) {
        this.user = user;
        this.roleName = user.getRole().name();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.displayName = user.getDisplayName();
        this.accountStatus = user.getAccountStatus();
    }

    public User getUser() {
        return user;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"Suspended".equalsIgnoreCase(accountStatus) &&
                !"Locked".equalsIgnoreCase(accountStatus);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !"Inactive".equalsIgnoreCase(accountStatus) &&
                !"Disabled".equalsIgnoreCase(accountStatus);
    }
}
