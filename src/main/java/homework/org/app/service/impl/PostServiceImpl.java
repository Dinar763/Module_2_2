package homework.org.app.service.impl;

import homework.org.app.exception.ServiceException;
import homework.org.app.model.Post;
import homework.org.app.repository.PostRepository;
import homework.org.app.service.PostService;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class PostServiceImpl implements PostService {
    private final PostRepository repository;

    public PostServiceImpl(PostRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Repository must not be null");
    }

    @Override
    public Post getByID(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        try {
            return repository.getById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to find label ", e);
        }
    }

    @Override
    public List<Post> getAll() {
        try {
            return repository.getAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get all posts", e);
        }
    }

    @Override
    public Post save(Post post) {
        if (post == null || post.getContent() == null || post.getWriter() == null) {
            throw new IllegalArgumentException("Post content and writer must be specified");
        }
        if (post.getWriter().getId() == null) {
            throw new IllegalArgumentException("Writer must be persisted before post creation");
        }
        try {
            return repository.save(post);
        } catch (SQLException e) {
            throw new ServiceException("Failed to save post", e);
        }
    }

    @Override
    public Post update(Post post) {
        try {
            return repository.update(post);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update post", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        try {
            repository.deleteById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete by id post", e);
        }
    }
}
