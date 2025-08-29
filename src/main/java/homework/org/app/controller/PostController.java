package homework.org.app.controller;

import homework.org.app.model.Post;
import homework.org.app.service.PostService;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PostController {
    private final PostService postService;

    public Post getByID(Long id) {
        return postService.getByID(id);
    }

    public List<Post> getAll() {
        List<Post> posts = postService.getAll();
        return posts;
    }

    public Post save(Post post) {
        return postService.save(post);
    }

    public Post update(Post post) {
        return postService.update(post);
    }

    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        postService.deleteById(id);
    }
}
