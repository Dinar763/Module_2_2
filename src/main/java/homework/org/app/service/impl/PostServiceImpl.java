package homework.org.app.service.impl;

import homework.org.app.model.Post;
import homework.org.app.repository.PostRepository;
import homework.org.app.service.PostService;

import java.util.List;
import java.util.Objects;

public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = Objects.requireNonNull(postRepository,
                "Repository must not be null");
    }

    @Override
    public Post getByID(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        return postRepository.getById(id);
    }

    @Override
    public List<Post> getAll() {
        return postRepository.getAll();
    }

    @Override
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public Post update(Post post) {
        return postRepository.updateWithLabels(post);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        postRepository.deleteById(id);
    }
}
