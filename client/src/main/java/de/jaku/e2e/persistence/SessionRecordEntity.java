package de.jaku.e2e.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private long id;

    private String name;

    private int deviceId;

    private byte[] serializedSessionRecord;
}
