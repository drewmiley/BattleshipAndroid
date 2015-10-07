package com.scottlogic.dmiley.battleship.logic.event;

import java.util.EventObject;

// Ship Hit Event
public class ShipHitEvent extends EventObject {

  public ShipHitEvent(Object source) {
    super(source);
  }
}
