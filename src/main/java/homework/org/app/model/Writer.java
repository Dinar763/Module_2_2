package homework.org.app.model;

import lombok.*;

import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Writer {
    @EqualsAndHashCode.Include
    private Long id;
    private String firstname;
    private String lastname;

    @ToString.Exclude
    private List<Post> posts;
    private Status status = Status.ACTIVE;
}
