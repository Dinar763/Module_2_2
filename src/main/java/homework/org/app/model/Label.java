package homework.org.app.model;

import lombok.*;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Label {

    @EqualsAndHashCode.Include
    private Long id;
    private String name;
    private Status status = Status.ACTIVE;
}
