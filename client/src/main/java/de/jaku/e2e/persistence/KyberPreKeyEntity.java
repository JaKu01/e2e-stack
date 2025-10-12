package de.jaku.e2e.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kyber_pre_keys")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class KyberPreKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 8192)
    private byte[] serializedKyberPreKey;
}
