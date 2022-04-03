package towersim.control;

import towersim.aircraft.Aircraft;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/***
 *  <p>
 *      Represents a first-in-first-out (FIFO) queue of aircraft waiting to take off.
 *
 *      FIFO ensures that the order in which aircraft are allowed to take
 *      off is based on long they have been waiting in the queue.
 *      An aircraft that has been waiting for longer than another aircraft
 *      will always be allowed to take off before the other aircraft.
 *  </p>
 */
public class TakeoffQueue extends AircraftQueue {

    /**takeoff queue Queue object */
    private Queue<Aircraft> takeOffQueue;

    /***
     * Constructs a new TakeoffQueue with an initially empty queue of aircraft.
     */
    public TakeoffQueue() {
        takeOffQueue = new LinkedList<Aircraft>();
    }

    /***
     * Adds the given aircraft to the queue.
     * @param aircraft aircraft to add to queue
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        takeOffQueue.add(aircraft);
    }

    /***
     * Returns the aircraft at the front of the queue without removing it from the queue,
     * or null if the queue is empty.
     *
     * Aircraft returned by peekAircraft() should be in the same order that they were
     * added via addAircraft().
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft peekAircraft() {
        return takeOffQueue.peek();
    }

    /***
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     *
     * Aircraft returned by removeAircraft() should be in the
     * same order that they were added via addAircraft().
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft removeAircraft() {
        if (takeOffQueue.isEmpty()) {
            return null;
        }
        Aircraft aircraft = peekAircraft();
        takeOffQueue.remove();
        return aircraft;
    }

    /***
     * <p>
     *     Returns a list containing all aircraft in the queue, in order.
     *
     *     That is, the first element of the returned list should be the first aircraft that
     *     would be returned by calling removeAircraft(), and so on.
     *
     *     Adding or removing elements from the returned list should not affect the original queue.
     * </p>
     * @return list of all aircraft in queue, in queue order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        Queue<Aircraft> takeOff2 = new LinkedList<>(takeOffQueue);
        List<Aircraft> orderedTakeOffAircrafts = new ArrayList<>();
        while (takeOffQueue.size() > 0) {
            orderedTakeOffAircrafts.add(removeAircraft());
        }
        this.takeOffQueue = takeOff2;
        return orderedTakeOffAircrafts;
    }


    /***
     * returns true if the takeOffQueue contains the aircraft
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    @Override
    public boolean containsAircraft(Aircraft aircraft) {
        return takeOffQueue.contains(aircraft);
    }


}
