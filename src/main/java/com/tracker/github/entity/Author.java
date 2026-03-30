package com.tracker.github.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String username;

    @Column(name = "pushed_at")
    private LocalDateTime pushedAt;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Commit> commits = new ArrayList<>();

    public Author() {}

    public Author(String name, String email, String username) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.pushedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public LocalDateTime getPushedAt() { return pushedAt; }
    public void setPushedAt(LocalDateTime pushedAt) { this.pushedAt = pushedAt; }
    public List<Commit> getCommits() { return commits; }
    public void setCommits(List<Commit> commits) { this.commits = commits; }
}
