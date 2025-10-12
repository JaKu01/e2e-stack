package de.jaku.e2e.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "session_records")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SessionRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int deviceId;

    @Column(nullable = false, length = 8192)
    private byte[] serializedSessionRecord;
}
