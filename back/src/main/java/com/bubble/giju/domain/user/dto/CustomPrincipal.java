package com.bubble.giju.domain.user.dto;

import com.bubble.giju.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

//public class CustomPrincipal implements OAuth2User, UserDetails {
public class CustomPrincipal implements UserDetails {

    private final User user;

    public CustomPrincipal(User user) {
        this.user = user;
    }

    // Custom
    public String getUserId() {
        return user.getUserId().toString();
    }

    public String getRole() {
        return user.getRole().toString();
    }

    // 공통
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_" + user.getRole().toString();
            }
        });

        return authorities;
    }

    // OAuth2User
//    @Override
//    public Map<String, Object> getAttributes() {
//        return Map.of();
//    }
//
//    @Override
//    public String getName() {
//        return user.getName();
//    }

    // UserDetails
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getLoginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
