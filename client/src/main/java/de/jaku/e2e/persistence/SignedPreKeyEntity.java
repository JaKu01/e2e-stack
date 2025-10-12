package de.jaku.e2e.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "signed_pre_keys")
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class SignedPreKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 8192)
    private byte[] serializedSignedPreKey;
}
