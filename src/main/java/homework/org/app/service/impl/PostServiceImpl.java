package homework.org.app.service.impl;

import homework.org.app.model.Post;
import homework.org.app.repository.PostRepository;
import homework.org.app.service.PostService;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor
public class PostServiceImpl implements PostService {
    @NonNull
    private final PostRepository postRepository;

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
