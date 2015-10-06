package com.scottlogic.dmiley.battleship.logic.event;

import java.util.EventObject;

// Ship Sunk Event
public class ShipSankEvent extends EventObject {

  public ShipSankEvent(Object source) {
    super(source);
  }
}
