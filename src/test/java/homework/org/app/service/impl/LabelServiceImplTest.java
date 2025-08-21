package homework.org.app.service.impl;

import homework.org.app.exception.NotFoundException;
import homework.org.app.exception.ServiceException;
import homework.org.app.model.Label;
import homework.org.app.model.Status;
import homework.org.app.repository.LabelRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelServiceImplTest {

    @Mock
    private LabelRepository mockRepository;

    private LabelServiceImpl labelService;

    private final Label testLabel1 = new Label(1L, "Label Test1", Status.ACTIVE);
    private final Label testLabel2 = new Label(2L, "Label Test1", Status.ACTIVE);
    private final List<Label> labelList = List.of(testLabel1, testLabel2);

    @BeforeEach
    void setup() {
        labelService = new LabelServiceImpl(mockRepository);
    }

    @Test
    void getByID_ShouldReturnLabelIdWhenLabelExists() throws SQLException {
        when(mockRepository.getById(1L)).thenReturn(testLabel1);
        Label result = labelService.getByID(1L);
        assertNotNull(result);
        assertEquals(testLabel1.getId(), result.getId());
        assertEquals(testLabel1.getName(), result.getName());
    }

    @Test
    void getByID_ShouldThrowServiceExeptionWhenIdIsNull() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> labelService.getByID(null));
        assertEquals("ID must be not null", exception.getMessage());
    }

    @Test
    void getByID_ShouldThrowExceptionWhenIdIsNotExist() throws SQLException {
        when(mockRepository.getById(150L)).thenReturn(null);
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> labelService.getByID(150L));
        assertEquals("Label with id 150 not found", exception.getMessage());
    }

    @Test
    void getALL_ShouldReturnListOfLabelsWhenTheseLabelsExists() throws SQLException {
        when(mockRepository.getAll()).thenReturn(labelList);
        List<Label> list = labelService.getAll();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(labelList, list);
    }

    @Test
    void getAll_ShouldThrowServiceExceptionWhenRepoFailure() throws SQLException {
        when(mockRepository.getAll()).thenThrow(new SQLException("DB error"));
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            labelService.getAll();
        });
        assertInstanceOf(SQLException.class, exception.getCause());
        assertEquals("Failed to get all labels", exception.getMessage());
    }

    @Test
    void save_ShouldReturnSavedLabel() throws SQLException {
        Label labelToSave = new Label(null, "New Label", Status.ACTIVE);
        Label savedLabel = new Label(10L, "New Label", Status.ACTIVE);
        when(mockRepository.save(labelToSave)).thenReturn(savedLabel);
        Label result = labelService.save(labelToSave);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(savedLabel.getName(), result.getName());
    }

    @Test
    void save_ShouldThtowServiceExceptionWhenRepoFalure() throws SQLException {
        when(mockRepository.save(testLabel1)).thenThrow(new SQLException("DB error"));
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            labelService.save(testLabel1);
        });
        assertInstanceOf(SQLException.class, exception.getCause());
        assertEquals("Failed to save label", exception.getMessage());
    }

    @Test
    void update_ShouldReturnUpdatedLabel() throws SQLException {
        Label originalLabel = new Label(1L, "qwerty", Status.ACTIVE);
        Label updateRequest = new Label(1L, "asdf", Status.ACTIVE);
        when(mockRepository.getById(1L)).thenReturn(originalLabel);
        when(mockRepository.update(updateRequest)).thenReturn(updateRequest);
        Label result = labelService.update(updateRequest);
        assertEquals("asdf", result.getName());
    }
}