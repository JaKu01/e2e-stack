package de.jaku.e2e.persistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pre_keys")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PreKeyEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 8192)
    private byte[] serializedPreKey;
}
