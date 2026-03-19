package elevator.model;

import java.io.Serializable;

import elevator.ElevatorGlobalValues;
import elevator.event.*;

public class Person extends Thread implements Serializable, ElevatorGlobalValues {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int personId = 1;

	private boolean isMove = false;

	private Location location;

	private PersonMoveListener personMoveListener;

	public Person(int identifier, Location initialLocation) {
		super();

		setPersonId(identifier);
		location = initialLocation;
		isMove = true;
	}

	public void setPersonMoveListener(PersonMoveListener listener) {
		personMoveListener = listener;
	}

	private void setLocation(Location newLocation) {
		location = newLocation;
	}

	private Location getLocation() {
		return location;
	}

	public void setMoving(boolean personMoving) {
		isMove = personMoving;
	}

	public boolean isMove() {
		return isMove;
	}

	public void run() {

		sendPersonMoveEvent(PERSON_CREATED);

		pauseThread(TIME_TO_WALK);

		setMoving(false);

		sendPersonMoveEvent(PERSON_ARRIVED);

		Door currentFloorDoor = location.getDoor();

		Elevator elevator = ((Floor) getLocation()).getElevatorShaft().getElevator();

		synchronized (currentFloorDoor) {

			if (!currentFloorDoor.isDoorOpen()) {

				sendPersonMoveEvent(PERSON_PRESSING_BUTTON);
				pauseThread(1000);

				Button floorButton = getLocation().getButton();
				floorButton.pressButton(getLocation());
			}

			try {

				while (!currentFloorDoor.isDoorOpen())
					currentFloorDoor.wait();
			}

			catch (InterruptedException interruptedException) {
				interruptedException.printStackTrace();
			}

			pauseThread(1000);

			synchronized (elevator) {

				sendPersonMoveEvent(PERSON_ENTERING_ELEVATOR);

				setLocation(elevator);

				pauseThread(1000);

				sendPersonMoveEvent(PERSON_PRESSING_BUTTON);
				pauseThread(1000);

				Button elevatorButton = getLocation().getButton();

				elevatorButton.pressButton(location);

				pauseThread(1000);
			}

		}

		synchronized (elevator) {

			synchronized (getLocation().getDoor()) {

				try {

					while (!getLocation().getDoor().isDoorOpen()) {
						getLocation().getDoor().wait();

					}

				}

				catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}

				pauseThread(1000);

				setLocation(elevator.getCurrentFloor());

				setMoving(true);

				sendPersonMoveEvent(PERSON_EXITING_ELEVATOR);

			}

		}

		pauseThread(2 * TIME_TO_WALK);

		sendPersonMoveEvent(PERSON_EXITED);

	}

	private void pauseThread(int milliseconds) {
		try {
			sleep(milliseconds);
		}

		catch (InterruptedException interruptedException) {
			interruptedException.printStackTrace();
		}
	}

	private void sendPersonMoveEvent(int eventType) {

		if (personMoveListener == null) {
			return;
		}

		PersonMoveEvent event = new PersonMoveEvent(this, getLocation(), getPersonId());

		switch (eventType) {

		case PERSON_CREATED:
			personMoveListener.personCreated(event);
			break;

		case PERSON_ARRIVED:
			personMoveListener.personArrived(event);
			break;

		case PERSON_ENTERING_ELEVATOR:
			personMoveListener.personEntered(event);
			break;

		case PERSON_PRESSING_BUTTON:
			personMoveListener.personPressedButton(event);
			break;

		case PERSON_EXITING_ELEVATOR:
			personMoveListener.personDeparted(event);
			break;

		case PERSON_EXITED:
			personMoveListener.personExited(event);
			break;

		default:
			break;
		}
	}

	public int getPersonId() {
		return personId;
	}

	public void setPersonId(int id) {
		this.personId = id;
	}

}
