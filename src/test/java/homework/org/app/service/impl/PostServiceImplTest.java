package homework.org.app.service.impl;

import homework.org.app.model.Label;
import homework.org.app.model.Post;
import homework.org.app.model.Status;
import homework.org.app.repository.PostRepository;
import homework.org.app.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository mockRepository;

    private PostService postService;

    private final Label testLabel1 = new Label(1L, "testLabel1");
    private final Post testPost1 = new Post(1L, "Post Test1", null, null,
            null, List.of(testLabel1), Status.ACTIVE);
    private final Post testPost2 = new Post(2L, "Post Test2", null,
            null, null, List.of(testLabel1), Status.ACTIVE);


    @BeforeEach
    void setup() {
        postService = new PostServiceImpl(mockRepository);
    }

    @Test
    void testGetByID() {
        when(mockRepository.getById(1L)).thenReturn(testPost1);
        Post result = postService.getByID(1L);
        assertEquals("Post Test1", result.getContent());
        verify(mockRepository).getById(1L);
    }

    @Test
    void testGetAll() {
        List<Post> posts = List.of(testPost1, testPost2);
        when(mockRepository.getAll()).thenReturn(posts);
        List<Post> result = postService.getAll();
        assertEquals(2, result.size());
        verify(mockRepository).getAll();
    }

    @Test
    void testSave() {
        Post newPost = new Post(null, "New Post", null, null, null, List.of(testLabel1), Status.ACTIVE);
        Post savedPost = new Post(3L, "New Post", null, null, null, List.of(testLabel1), Status.ACTIVE);

        when(mockRepository.save(newPost)).thenReturn(savedPost);
        Post result = postService.save(newPost);
        assertEquals(3L, result.getId());
        verify(mockRepository).save(newPost);
    }

    @Test
    void testUpdate() {
        when(mockRepository.updateWithLabels(testPost1)).thenReturn(testPost1);
        Post result = postService.update(testPost1);
        assertEquals("Post Test1", result.getContent());
        verify(mockRepository).updateWithLabels(testPost1);
    }

    @Test
    void testDeleteByID() {
        postService.deleteById(1L);
        verify(mockRepository).deleteById(1L);
    }
}