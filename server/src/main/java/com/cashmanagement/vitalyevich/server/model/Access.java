package com.cashmanagement.vitalyevich.server.model;

import javax.persistence.*;

@Entity
@Table(name = "access")
public class Access {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "login", nullable = false, length = 20)
    private String login;

    @Column(name = "user_password", nullable = false)
    private String userPassword;

    @Column(name = "active", nullable = false)
    private Boolean active = false;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Access() {
    }

    public Access(User user, String login, String userPassword, Boolean active) {
        this.user = user;
        this.login = login;
        this.userPassword = userPassword;
        this.active = active;
    }

    public Access(Integer id, User user, String login, String userPassword, Boolean active) {
        this.id = id;
        this.user = user;
        this.login = login;
        this.userPassword = userPassword;
        this.active = active;
    }
}