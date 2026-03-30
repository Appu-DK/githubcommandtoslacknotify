package com.tracker.github.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commits")
public class Commit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commit_id")
    private String commitId;

    private String message;

    private String url;

    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    public Commit() {}

    public Commit(String commitId, String message, String url, LocalDateTime timestamp, Author author) {
        this.commitId = commitId;
        this.message = message;
        this.url = url;
        this.timestamp = timestamp;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }
}
