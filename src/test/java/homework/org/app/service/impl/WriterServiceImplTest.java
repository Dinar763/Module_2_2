package homework.org.app.service.impl;

import homework.org.app.model.Label;
import homework.org.app.model.Post;
import homework.org.app.model.Status;
import homework.org.app.model.Writer;
import homework.org.app.repository.WriterRepository;
import homework.org.app.service.WriterService;
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
class WriterServiceImplTest {

    @Mock
    private WriterRepository mockRepository;

    private WriterService writerService;

    private final Label testLabel1 = new Label(1L, "testLabel1");
    private final Post testPost1 = new Post(1L, "Post Test1", null, null,
            null, List.of(testLabel1), Status.ACTIVE);
    private final Writer testWriter1 = new Writer(1L, "John", "Doe", List.of(testPost1));
    private final Writer testWriter2 = new Writer(2L, "Jane", "Smith", List.of());

    @BeforeEach
    void setup() {
        writerService = new WriterServiceImpl(mockRepository);
    }

    @Test
    void testGetByID() {
        when(mockRepository.getById(1L)).thenReturn(testWriter1);
        Writer result = writerService.getByID(1L);
        assertEquals("John", result.getFirstname());
        assertEquals("Doe", result.getLastname());
        verify(mockRepository).getById(1L);
    }

    @Test
    void testGetAll() {
        List<Writer> writers = List.of(testWriter1, testWriter2);
        when(mockRepository.getAll()).thenReturn(writers);
        List<Writer> result = writerService.getAll();
        assertEquals(2, result.size());
        verify(mockRepository).getAll();
    }

    @Test
    void testSave() {
        Writer newWriter = new Writer(null, "New", "Writer", List.of());
        Writer savedWriter = new Writer(3L, "New", "Writer", List.of());

        when(mockRepository.save(newWriter)).thenReturn(savedWriter);
        Writer result = writerService.save(newWriter);
        assertEquals(3L, result.getId());
        verify(mockRepository).save(newWriter);
    }

    @Test
    void testUpdate() {
        when(mockRepository.update(testWriter1)).thenReturn(testWriter1);
        Writer result = writerService.update(testWriter1);
        assertEquals("John", result.getFirstname());
        verify(mockRepository).update(testWriter1);
    }

    @Test
    void testDeleteByID() {
        writerService.deleteById(1L);
        verify(mockRepository).deleteById(1L);
    }
}