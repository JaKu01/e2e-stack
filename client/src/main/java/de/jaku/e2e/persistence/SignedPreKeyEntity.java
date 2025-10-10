package de.jaku.e2e.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private long id;

    @Column(nullable = false, length = 8192)
    private byte[] serializedSignedPreKey;
}
