package me.fefo.betterjails.common.events;

import java.sql.Time;
import java.util.UUID;

public abstract class BJEvent {
    UUID operatorUUID;
    UUID victimUUID;
    Time timestamp;
}
