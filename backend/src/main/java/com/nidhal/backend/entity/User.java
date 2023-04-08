package com.nidhal.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "user_app",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        })
public class User implements UserDetails {
    @Id
    @SequenceGenerator(
            name = "user_generator",
            sequenceName = "user_app_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @Column(name = "id")
    private Long id;

    @Column(length = 30)
    private String firstName;

    @Column(length = 30)
    private String lastName;

    @Column(length = 100)
    private String email;

    private String password;

    @Transient
    private String confirmPassword;


    @Enumerated(EnumType.STRING)
    private Role role;


    /**
     * the user by default is not enable, until he activates his account.
     */
    @Column(name = "enabled")
    private boolean enabled = false;

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;

    public static User of(String firstName, String lastName, String email, String password, String confirmPassword, Role role) {
        return new User(null, firstName, lastName, email, password, confirmPassword, role, false, null);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPassword() {
        return password;
    }


    public String getUsername() {
        return email;
    }


    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(role.toString()));
        return authorities;
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

}
