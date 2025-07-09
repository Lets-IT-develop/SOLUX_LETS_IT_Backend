package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Area {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Area parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Area> subAreas;

    // init용 Constructor
    public Area(Long id, String name, Area parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }
}
