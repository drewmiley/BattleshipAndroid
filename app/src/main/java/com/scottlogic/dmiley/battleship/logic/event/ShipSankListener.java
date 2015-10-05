package com.scottlogic.dmiley.battleship.logic.event;

// Ship Sunk Event Interface
public interface ShipSankListener {

    public void onShipSank(ShipSankEvent shipSankEvent);
}
