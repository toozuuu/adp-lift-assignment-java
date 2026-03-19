package elevator.event;

import elevator.model.*;

public class PersonMoveEvent extends ElevatorSimulationEvent {

	private int id;

	public PersonMoveEvent(Object source, Location location, int personId) {
		super(source, location);
		id = personId;
	}

	public int getID() {
		return (id);
	}
}
