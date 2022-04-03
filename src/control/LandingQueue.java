package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.PassengerAircraft;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/***
 * <p>
 *     Represents a rule-based queue of aircraft waiting in the air to land.
 *
 *     The rules in the landing queue are designed to ensure that aircraft are prioritised
 *     for landing based on "urgency" factors such as remaining fuel onboard,
 *     emergency status and cargo type.
 * </p>
 */
public class LandingQueue extends AircraftQueue {

    /** Landing queue of aircraft waiting to land */
    private Queue<Aircraft> landingQueue;

    /***
     * Constructs a new LandingQueue with an initially empty queue of aircraft.
     */
    public LandingQueue() {
        landingQueue = new LinkedList<Aircraft>();
    }

    /***
     * Adds the given aircraft to the queue.
     * @param aircraft aircraft to add to queue
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        landingQueue.add(aircraft);
    }

    /***
     * Returns the aircraft at the front of the queue without removing it from the queue,
     * or null if the queue is empty.
     *
     * <p>
     *      The rules for determining which aircraft in the
     *      queue should be returned next are as follows:
     *
     *     If an aircraft is currently in a state of emergency, it should be returned.
     *     If more than one aircraft are in an emergency, return the one added to the queue first.
     *     If an aircraft has less than or equal to 20 percent fuel remaining, a critical level,
     *      it should be returned (see Aircraft.getFuelPercentRemaining()).
     *     If more than one aircraft have a critical level of fuel onboard,
     *      return the one added to the queue first.
     *     If there are any passenger aircraft in the queue,
     *      return the passenger aircraft that was added to the queue first.
     *     If this point is reached and no aircraft has been returned,
     *      return the aircraft that was added to the queue first.
     * </p>
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft peekAircraft() {

        if (landingQueue.isEmpty()) {
            return null;
        }

        //Fuel amount minimum, if aircraft is <=20% remaining
        int minFuelPercent = 20;

        //Checking if aicraft is in emergency state
        for (Aircraft landingQueues : landingQueue) {
            Queue<Aircraft> countList = new LinkedList<>();
            if (landingQueues.hasEmergency()) {
                countList.add(landingQueues);
                return countList.peek();
            }
        }
        //checking if fuel amount is critical
        for (Aircraft landingQueues : landingQueue) {
            Queue<Aircraft> countList2 = new LinkedList<Aircraft>();
            if (landingQueues.getFuelPercentRemaining() <= minFuelPercent) {
                countList2.add(landingQueues);
                return countList2.peek();
            }
        }
        //checking if it is a passenger aircraft
        for (Aircraft landingQueues : landingQueue) {
            Queue<Aircraft> countList3 = new LinkedList<Aircraft>();
            if (landingQueues instanceof PassengerAircraft) {
                countList3.add(landingQueues);
                return countList3.peek();
            }
        }
        return landingQueue.peek();
    }

    /***=
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     *
     * The same rules as described in peekAircraft() should be
     * used for determining which aircraft to remove and return.
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft removeAircraft() {
        if (landingQueue.isEmpty()) {
            return null;
        }
        //return the aircraft that has been removed
        // but also remove said aircraft from the queue
        Aircraft aircraft = peekAircraft();
        landingQueue.remove(aircraft);
        return aircraft;
    }

    /***
     * Returns a list containing all aircraft in the queue, in order.
     * That is, the first element of the returned list should be the first aircraft that would
     * be returned by calling removeAircraft(), and so on.
     *
     * Adding or removing elements from the returned list should not affect the original queue.
     *
     * @return list of all aircraft in queue, in queue order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        Queue<Aircraft> landing2 = new LinkedList<>(landingQueue);
        List<Aircraft> orderedLandingAircraft = new LinkedList<>();

        //if there are aircraft in the queue,
        // remove them and return an ordered list
        while (landingQueue.size() > 0) {
            orderedLandingAircraft.add(removeAircraft());
        }
        this.landingQueue = landing2;
        return orderedLandingAircraft;
    }


    /***
     *Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    @Override
    public boolean containsAircraft(Aircraft aircraft) {
        return landingQueue.contains(aircraft);
    }
}
