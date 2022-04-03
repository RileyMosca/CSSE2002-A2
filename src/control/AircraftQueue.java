package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.util.Encodable;

import java.util.List;



/***
 * <p>
 *  Abstract representation of a queue containing aircraft.
 *
 * Aircraft can be added to the queue, and aircraft at the front of
 * the queue can be queried or removed. A list of all aircraft contained in
 * the queue (in queue order) can be obtained.
 * The queue can be checked for containing a specified aircraft.
 *
 * The order that aircraft are removed from the queue depends on
 * the chosen concrete implementation of the AircraftQueue.
 * </p>
 */
public abstract class AircraftQueue implements Encodable {

    /***
     * Adds given aircraft to the queue
     * @param aircraft aircraft to add to queue
     */
    public abstract void addAircraft(Aircraft aircraft);

    /***
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     * @return aircraft at front of queue
     */
    public abstract Aircraft removeAircraft();

    /***
     * Returns the aircraft at the front of the queue without removing
     * it from the queue, or null if the queue is empty.
     * @return aircraft at front of queue
     */
    public abstract Aircraft peekAircraft();

    /***
     * <p>
     * Returns a list containing all aircraft in the queue, in order.
     *
     * That is, the first element of the returned list should be the first aircraft that would
     * be returned by calling removeAircraft(), and so on.
     *
     * Adding or removing elements from the returned list should not affect the original queue.
     * </p>
     * @return list of all aircraft in queue, in queue order
     */
    public abstract List<Aircraft> getAircraftInOrder();

    /***
     * Returns true if given aircraft is in the queue
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    public abstract boolean containsAircraft(Aircraft aircraft);

    /***
     * Returns the human readable string representation of this aircraft queue
     *
     *  The format of the string to return is:
     *     QueueType [callsign1, callsign2, ..., callsignN]
     *
     * @return string representation of this aircraft queue
     */
    public String toString() {
        StringBuilder toStringQueue = new StringBuilder();
        for (int i = 0; i < getAircraftInOrder().size(); i++) {
            toStringQueue.append(", ");
            toStringQueue.append(getAircraftInOrder().get(i).getCallsign());
        }
        toStringQueue.replace(0, 2, "");
        return String.format("%s [%s]",
                getClass().getSimpleName(),
                toStringQueue.toString());
    }

    /***
     * Returns the machine-readable string representation of this aircraft queue.
     *  The format of the string to return is
     *
     * QueueType:numAircraft:
     *      callsign1,callsign2,...,callsignN
     *
     * @return encoded string representation of this aircraft queue
     */
    public String encode() {
        if (getAircraftInOrder().isEmpty()) {
            return String.format("%s:%d", getClass().getSimpleName(), 0);
        }
        StringBuilder encodedQueue = new StringBuilder();
        for (int i = 0; i < getAircraftInOrder().size(); i++) {
            encodedQueue.append(",");
            encodedQueue.append(getAircraftInOrder().get(i).getCallsign());
        }
        encodedQueue.replace(0, 1, "");
        return String.format("%s:%d\n%s",
                getClass().getSimpleName(),
                getAircraftInOrder().size(),
                encodedQueue.toString());
    }
}

