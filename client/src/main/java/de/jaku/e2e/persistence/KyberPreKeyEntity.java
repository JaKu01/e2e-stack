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
@Table(name = "kyber_pre_keys")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class KyberPreKeyEntity {
    @Id
    private long id;

    @Column(nullable = false)
    private byte[] serializedKyberPreKey;
}
