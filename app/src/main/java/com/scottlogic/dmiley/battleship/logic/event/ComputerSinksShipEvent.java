package com.scottlogic.dmiley.battleship.logic.event;

import java.util.EventObject;


public class ComputerSinksShipEvent extends EventObject {

  public ComputerSinksShipEvent(Object source) {
    super(source);
  }
}
