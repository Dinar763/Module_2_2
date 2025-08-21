package homework.org.app.controller.impl;

import homework.org.app.controller.PostController;
import homework.org.app.model.Post;
import homework.org.app.service.PostService;

import java.util.List;

public class PostControllerImpl implements PostController {
    private final PostService postService;

    public PostControllerImpl(PostService postService) {
        this.postService = postService;
    }

    @Override
    public Post getByID(Long id) {
        return postService.getByID(id);
    }

    @Override
    public List<Post> getAll() {
        return postService.getAll();
    }

    @Override
    public Post save(Post post) {
        return postService.save(post);
    }

    @Override
    public Post update(Post post) {
        return postService.update(post);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        postService.deleteById(id);
    }
}
