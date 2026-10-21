package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_bookings")
public class RoomBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** Champ de verrouillage optimiste : incrémenté par Hibernate à chaque UPDATE. */
    @Version
    @Column(name = "version", nullable = false)
    private int version;

    public RoomBooking() {}

    public RoomBooking(String roomName, LocalDateTime startTime, LocalDateTime endTime) {
        this.roomName = roomName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() { return id; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public int getVersion() { return version; }

    @Override
    public String toString() {
        return "RoomBooking{id=" + id + ", room='" + roomName + "', version=" + version + "}";
    }
}
