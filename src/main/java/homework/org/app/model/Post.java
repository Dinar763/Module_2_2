package homework.org.app.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @EqualsAndHashCode.Include
    private Long id;
    private String content;
    private LocalDateTime created;
    private LocalDateTime updated;

    //@ToString.Exclude
    private Writer writer;

    //@ToString.Exclude
    private List<Label> labels = new ArrayList<>();
    private Status status = Status.ACTIVE;
}