package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftType;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;
import towersim.util.Tickable;
import java.util.*;
import static towersim.tasks.TaskType.*;

/**
 * Represents a the control tower of an airport.
 * <p>
 * The control tower is responsible for managing the operations of the airport, including arrivals
 * and departures in/out of the airport, as well as aircraft that need to be loaded with cargo
 * at gates in terminals.
 * @ass1
 */
public class ControlTower implements Tickable {
    /** List of all aircraft managed by the control tower. */
    private final List<Aircraft> aircraft;

    /** List of all terminals in the airport. */
    private final List<Terminal> terminals;

    /**Takeoff queue for all aircraft awaiting takeoff */
    private TakeoffQueue takeOffQueue;

    /**Landing queue for all aircraft awaiting to land */
    private LandingQueue landingQueue;

    /** Map of all aircraft integer pairs pof aircraft to their loading time */
    private HashMap<Aircraft, Integer> loadingAircraft;

    /** ticks elapsed in the simulation */
    private long ticksElapsed;


    /***
     * Creates a new ControlTower.
     *
     * @param ticksElapsed number of ticks that have elapsed since the tower was first created
     * @param aircraft list of aircraft managed by the control tower
     * @param landingQueue queue of aircraft waiting to land
     * @param takeoffQueue queue of aircraft waiting to take off
     * @param loadingAircraft mapping of aircraft that are loading cargo to the number
     *                       of ticks remaining for loading
     */
    public ControlTower(long ticksElapsed, List<Aircraft> aircraft, LandingQueue landingQueue,
                        TakeoffQueue takeoffQueue, Map<Aircraft, Integer> loadingAircraft) {
        this.aircraft = new ArrayList<>();
        this.terminals = new ArrayList<>();
        this.loadingAircraft = new HashMap<Aircraft, Integer>();
        this.ticksElapsed = ticksElapsed;
        this.takeOffQueue = takeoffQueue;
        this.landingQueue = landingQueue;
    }

    /**
     * Adds the given terminal to the jurisdiction of this control tower.
     *
     * @param terminal terminal to add
     * @ass1
     */
    public void addTerminal(Terminal terminal) {
        this.terminals.add(terminal);
    }

    /**
     * Returns a list of all terminals currently managed by this control tower.
     * <p>
     * The order in which terminals appear in this list should be the same as the order in which
     * they were added by calling {@link #addTerminal(Terminal)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all terminals
     * @ass1
     */
    public List<Terminal> getTerminals() {
        return new ArrayList<>(this.terminals);
    }

    /**
     * Adds the given aircraft to the jurisdiction of this control tower.
     * <p>
     * If the aircraft's current task type is {@code WAIT} or {@code LOAD}, it should be parked at a
     * suitable gate as found by the {@link #findUnoccupiedGate(Aircraft)} method.
     * If there is no suitable gate for the aircraft, the {@code NoSuitableGateException} thrown by
     * {@code findUnoccupiedGate()} should be propagated out of this method.
     *
     * @param aircraft aircraft to add
     * @throws NoSuitableGateException if there is no suitable gate for an aircraft with a current
     *                                 task type of {@code WAIT} or {@code LOAD}
     * @ass1
     */
    public void addAircraft(Aircraft aircraft) throws NoSuitableGateException {
        TaskType currentTaskType = aircraft.getTaskList().getCurrentTask().getType();
        if (currentTaskType == WAIT || currentTaskType == TaskType.LOAD) {
            Gate gate = findUnoccupiedGate(aircraft);
            try {
                gate.parkAircraft(aircraft);
            } catch (NoSpaceException ignored) {
                // not possible, gate unoccupied
            }
        }
        this.aircraft.add(aircraft);
        placeAircraftInQueues(aircraft);
    }

    /**
     * Returns a list of all aircraft currently managed by this control tower.
     * <p>
     * The order in which aircraft appear in this list should be the same as the order in which
     * they were added by calling {@link #addAircraft(Aircraft)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all aircraft
     * @ass1
     */
    public List<Aircraft> getAircraft() {
        return new ArrayList<>(this.aircraft);
    }

    /**
     * Attempts to find an unoccupied gate in a compatible terminal for the given aircraft.
     * <p>
     * Only terminals of the same type as the aircraft's AircraftType (see
     * {@link towersim.aircraft.AircraftCharacteristics#type}) should be considered. For example,
     * for an aircraft with an AircraftType of {@code AIRPLANE}, only AirplaneTerminals may be
     * considered.
     * <p>
     * For each compatible terminal, the {@link Terminal#findUnoccupiedGate()} method should be
     * called to attempt to find an unoccupied gate in that terminal. If
     * {@code findUnoccupiedGate()} does not find a suitable gate, the next compatible terminal
     * in the order they were added should be checked instead, and so on.
     * <p>
     * If no unoccupied gates could be found across all compatible terminals, a
     * {@code NoSuitableGateException} should be thrown.
     *
     * @param aircraft aircraft for which to find gate
     * @return gate for given aircraft if one exists
     * @throws NoSuitableGateException if no suitable gate could be found
     * @ass1
     */
    public Gate findUnoccupiedGate(Aircraft aircraft) throws NoSuitableGateException {
        AircraftType aircraftType = aircraft.getCharacteristics().type;
        for (Terminal terminal : terminals) {
            /*
             * Only check for available gates at terminals that are of the same aircraft type as
             * the aircraft
             */
            if (!terminal.hasEmergency()) {
                if ((terminal instanceof AirplaneTerminal && aircraftType == AircraftType.AIRPLANE)
                        || (terminal instanceof HelicopterTerminal
                        && aircraftType == AircraftType.HELICOPTER)) {
                    try {
                        // This terminal found a gate, return it
                        return terminal.findUnoccupiedGate();
                    } catch (NoSuitableGateException e) {
                        // If this terminal has no unoccupied gates, try the next one
                    }
                }
            }
        }
        throw new NoSuitableGateException("No gate available for aircraft");
    }

    /**
     * Finds the gate where the given aircraft is parked, and returns null if the aircraft is
     * not parked at any gate in any terminal.
     *
     * @param aircraft aircraft whose gate to find
     * @return gate occupied by the given aircraft; or null if none exists
     * @ass1
     */
    public Gate findGateOfAircraft(Aircraft aircraft) {
        for (Terminal terminal : this.terminals) {
            for (Gate gate : terminal.getGates()) {
                if (Objects.equals(gate.getAircraftAtGate(), aircraft)) {
                    return gate;
                }
            }
        }
        return null;
    }

    /**
     * Advances the simulation by one tick.
     * <p>
     * On each tick, the control tower should call {@link Aircraft#tick()} on all aircraft managed
     * by the control tower.
     * <p>
     * Note that the actions performed by {@code tick()} are very simple at the moment and will be
     * expanded on in assignment 2.
     * @ass1
     */
    @Override
    public void tick() {
        //updating ticks elapses by 1 tick
        ticksElapsed += 1;

        // Call tick() on all other sub-entities
        for (Aircraft aircraft : this.aircraft) {
            aircraft.tick();

            // if task is wait/load move to next task
            if (aircraft.getTaskList().getCurrentTask().getType() == AWAY
                    || aircraft.getTaskList().getCurrentTask().getType() == WAIT) {
                aircraft.getTaskList().moveToNextTask();
            }

            //if task is load, call loadAircraft method
            if (aircraft.getTaskList().getCurrentTask().getType() == LOAD) {
                loadAircraft();
            }

            // every 2nd tick we attempt to land aircraft
            if (getTicksElapsed() % 2 == 0) {
                if (!tryLandAircraft()) {
                    tryTakeOffAircraft();
                }
            } else {
                tryTakeOffAircraft();
            }

            //Now place all aircraft in queues by calling the method
        }
        placeAllAircraftInQueues();
    }

    /***
     *  Returns the ticks elapsed of the simulation
     * @return ticks elapsed
     */
    public long getTicksElapsed() {
        return ticksElapsed;
    }

    /**
     * Returns the queue of aircraft waiting to take off.
     * @return takeoff queue
     */
    public AircraftQueue getTakeoffQueue() {
        return this.takeOffQueue;
    }

    /***
     * Returns the queue of aircraft waiting to land
     * @return landing queue
     */
    public AircraftQueue getLandingQueue() {
        return this.landingQueue;
    }

    /***
     * Returns the mapping of loading aircraft to their remaining load times.
     * @return loading aircraft map
     */
    public Map<Aircraft, Integer> getLoadingAircraft() {
        return this.loadingAircraft;
    }

    /***
     * Attempts to land one aircraft waiting in the landing queue and park it at a suitable gate.
     * @return true if an aircraft was successfully landed and parked; false otherwise
     */
    public boolean tryLandAircraft() {
        // if queue is empty, return null
        if (this.landingQueue.getAircraftInOrder().isEmpty()) {
            return false;

        // if size is valid, find a gate to land at
        } else if (this.landingQueue.getAircraftInOrder().size() >= 1) {
            Gate potentialGate = findGateOfAircraft(getLandingQueue().getAircraftInOrder().get(0));

            // if gate isn't null, try park the aircraft removed from queue at it
            if (potentialGate != null) {
                Aircraft removedFromQueue = this.landingQueue.removeAircraft();
                try {
                    potentialGate.parkAircraft(removedFromQueue);
                } catch (NoSpaceException e) {
                    return false;
                }

                // unload aircraft and move it to next task
                removedFromQueue.unload();
                removedFromQueue.getTaskList().moveToNextTask();
                return true;
            }
        }
        return false;
    }

    /***
     * Attempts to allow one aircraft waiting in the takeoff queue to take off.
     */
    public void tryTakeOffAircraft() {
        // if the take off queue isn't empty, move to next task and remove from queue
        if (!(this.getTakeoffQueue().getAircraftInOrder().isEmpty())) {
            this.getTakeoffQueue().peekAircraft().getTaskList().moveToNextTask();
            this.getTakeoffQueue().removeAircraft();
        }
    }

    /***
     * Updates the time remaining to load on all currently loading aircraft and removes aircraft
     * from their gate once finished loading.
     */
    //todo check this :)
    public void loadAircraft() {
        //checking the aircraft inteegr entries of the map to update the times ONLY
        for (Map.Entry<Aircraft, Integer> aircraft : loadingAircraft.entrySet()) {

            // update the loading time by decrementing by 1 tick
            loadingAircraft.replace(aircraft.getKey(), aircraft.getValue() - 1);


            // check if the value equals zero, remove aircraft from gate,
            // move it to next take, and move it from the map
            if (aircraft.getValue() == 0) {
                findGateOfAircraft(aircraft.getKey()).aircraftLeaves();
                aircraft.getKey().getTaskList().moveToNextTask();
                loadingAircraft.remove(aircraft.getKey());
            }
        }
    }

    /***
     * Calls placeAircraftInQueues(Aircraft) on all aircraft managed by the control tower.
     */
    public void placeAllAircraftInQueues() {
        for (Aircraft aircrafts : aircraft) {
            placeAircraftInQueues(aircrafts);
        }
    }

    /***
     * Moves the given aircraft to the appropriate queue based on its current task.
     * @param aircraft aircraft to move to appropriate queue
     */
    public void placeAircraftInQueues(Aircraft aircraft) {

        // if type is land, add to landing queue
        // if type is takeoff, add to takeoff queue
        switch (aircraft.getTaskList().getCurrentTask().getType()) {
            case LAND:
                if (!landingQueue.containsAircraft(aircraft)) {
                    landingQueue.addAircraft(aircraft);
                }
                break;

            case TAKEOFF:
                if (!takeOffQueue.containsAircraft(aircraft)) {
                    takeOffQueue.addAircraft(aircraft);
                }
                break;

            // if type is load, place aircraft/ time in the HashMap
            case LOAD:
                if (!loadingAircraft.containsKey(aircraft)) {
                    getLoadingAircraft().put(aircraft, aircraft.getLoadingTime());
                }
                break;
        }
    }

    /***
     * <p>
     *     Returns the human-readable string representation of this control tower.
     *
     * The format of the string to return is
     * ControlTower: numTerminals terminals, numAircraft total aircraft
     * (numLanding LAND, numTakeoff TAKEOFF, numLoad LOAD)
     * </p>
     * @return string representation of this control tower
     */
    public String toString() {
        return String.format("%s: %d %s %d %s (%d %s, %d %s, %d %s)",
                "ControlTower",
                terminals.size(),
                "terminals,",
                aircraft.size(),
                "total aircraft",
                getLandingQueue().getAircraftInOrder().size(),
                "LAND",
                getTakeoffQueue().getAircraftInOrder().size(),
                "TAKEOFF",
                loadingAircraft.size(),
                "LOAD");
    }
}
