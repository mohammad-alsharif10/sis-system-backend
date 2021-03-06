package com.sis.entity.security;

import com.sis.entity.BaseEntity;
import com.sis.entity.UserFile;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "username", unique = true)
    private String username;

    @NotNull
    @Email(message = "Email should be valid")
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "type")
    private String type;

    @Column(name = "token")
    private String token;

    @NotNull
    @Column(name = "password")
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role role;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private List<UserFile> userFileList = new ArrayList<>();

}
