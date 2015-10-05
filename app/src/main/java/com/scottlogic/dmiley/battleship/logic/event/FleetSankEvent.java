package com.scottlogic.dmiley.battleship.logic.event;

import java.util.EventObject;

// Fleet Sunk Event
public class FleetSankEvent extends EventObject {

    public FleetSankEvent(Object source) {
        super(source);
    }
}
