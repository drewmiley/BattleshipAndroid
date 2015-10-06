package com.scottlogic.dmiley.battleship.logic.event;

import com.scottlogic.dmiley.battleship.logic.event.FleetSankEvent;

// Fleet Sunk Event Interface
public interface FleetSankListener {
  public void onFleetSank(FleetSankEvent fleetSankEvent);
}
