package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import towersim.util.NoSpaceException;

import java.util.*;

/***
 * Utility class that contains static methods for loading a control tower
 * and associated entities from files.
 */
public class ControlTowerInitialiser {


    /***
     *
     * Loads the number of ticks elapsed from the given reader instance.
     * @param reader reader from which to load the number of ticks elapsed
     * @return number of ticks elapsed
     * @throws MalformedSaveException  if the format of the text read from the reader is invalid
     * @throws IOException if an IOException is encountered when reading from the reader
     */
    public static long loadTick(Reader reader) throws MalformedSaveException, IOException {
        long ticks;
        //Convert reader to buffered reader
        BufferedReader brTick = new BufferedReader(reader);
        String lineTick = brTick.readLine();

        //checking if the line is empty or null as a precursor
        if (lineTick == null) {
            throw new MalformedSaveException();
        }
        try {
            //trying to see if the data from the file can be parsed as a long
            ticks = Long.parseLong(lineTick);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }

        //trying to see if ticks are less than 0
        if (ticks < 0) {
            throw new MalformedSaveException();
        }
        return ticks;
    }

    /***
     * Loads the list of all aircraft managed by the control tower from the given reader instance.
     * @param reader reader from which to load the list of aircraft
     * @return list of aircraft read from the reader
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the given string is invalid
     */
    public static List<Aircraft> loadAircraft(Reader reader) throws IOException,
            MalformedSaveException {
        int firstLine;
        List<Aircraft> loadingAircraft = new LinkedList<>();
        BufferedReader brLoadAircraft = new BufferedReader(reader);
        String lineLoadAircraft = brLoadAircraft.readLine();
        String currentLine;

        if (lineLoadAircraft == null) {
            throw new MalformedSaveException();
        }


        try {
            //the first line should be an integer
            firstLine = Integer.parseInt(lineLoadAircraft);

            //variable for first line of doc
            if (firstLine == 0) {
                return loadingAircraft;
            }
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }


        //reading every non null line and adding to list of aircraft
        while ((currentLine = brLoadAircraft.readLine()) != null) {
            loadingAircraft.add(readAircraft(currentLine));
        }

        //first line should equal the amount of aircraft in the list
        if (firstLine != loadingAircraft.size()) {
            throw new MalformedSaveException();
        }
        return loadingAircraft;
    }

    /***
     * Loads the takeoff queue, landing queue and map of loading aircraft from the given reader
     * instance.
     * @param reader reader from which to load the queues and loading map
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param takeoffQueue empty takeoff queue that aircraft will be added to
     * @param landingQueue empty landing queue that aircraft will be added to
     * @param loadingAircraft empty map that aircraft and loading times will be added to
     * @throws MalformedSaveException if the format of the given string is invalid
     * @throws IOException if an IOException is encountered when reading from the reader
     */
    public static void loadQueues(Reader reader, List<Aircraft> aircraft,
                                  TakeoffQueue takeoffQueue, LandingQueue landingQueue,
                                  Map<Aircraft, Integer> loadingAircraft) throws
            MalformedSaveException, IOException {


        BufferedReader brLoadQueue = new BufferedReader(reader);

        readQueue(brLoadQueue, aircraft, takeoffQueue);

        readQueue(brLoadQueue, aircraft, landingQueue);

        readLoadingAircraft(brLoadQueue, aircraft, loadingAircraft);

    }

    /***
     * Loads the list of terminals and their gates from the given reader instance.
     * @param reader reader from which to load the list of terminals and their gates
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return list of terminals (with their gates) read from the reader
     * @throws MalformedSaveException if the format of the given string is invalid
     * @throws IOException if an IOException is encountered when reading from the reader
     */
    public static List<Terminal> loadTerminalsWithGates(Reader reader, List<Aircraft> aircraft)
            throws MalformedSaveException, IOException {

        List<Terminal> loadedTermsWithGates = new LinkedList<>();
        int terminalNumbers;

        BufferedReader brLoadTermsWithGates = new BufferedReader(reader);
        String lineLoadTermsWithGates = brLoadTermsWithGates.readLine();

        if (lineLoadTermsWithGates == null) {
            throw new MalformedSaveException();
        }

        //terminals at the top of file must be an integer
        try {
            terminalNumbers = Integer.parseInt(lineLoadTermsWithGates);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }
        if (terminalNumbers == 0) {
            return loadedTermsWithGates;
        }

        if (terminalNumbers < 0) {
            throw new MalformedSaveException();
        }

        //reading next line
        String currentLine;
        //checking subsequent lines until line is null
        while ((currentLine = brLoadTermsWithGates.readLine()) != null) {
            loadedTermsWithGates.add(readTerminal(currentLine, brLoadTermsWithGates,
                    aircraft));

        }
        return loadedTermsWithGates;
    }

    /***
     * Creates a control tower instance by reading various airport entities from the given readers.
     * @param tick reader from which to load the number of ticks elapsed
     * @param aircraft reader from which to load the list of aircraft
     * @param queues reader from which to load the aircraft queues and map of loading aircraft
     * @param terminalsWithGates reader from which to load the terminals and their gates
     * @return control tower created by reading from the given readers
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the given string is invalid
     */
    public static ControlTower createControlTower(Reader tick, Reader aircraft, Reader queues,
                                                  Reader terminalsWithGates) throws IOException,
            MalformedSaveException {

        long loadedTicks = loadTick(tick);
        List<Aircraft> aircraftFromLoad = loadAircraft(aircraft);
        TakeoffQueue takeoffQueue = new TakeoffQueue();
        LandingQueue landingQueue = new LandingQueue();
        List<Terminal> terminalsFromLoad =
                loadTerminalsWithGates(terminalsWithGates, aircraftFromLoad);
        TreeMap<Aircraft, Integer> loadedMap =
                new TreeMap<>(Comparator.comparing(Aircraft::getCallsign));
        loadQueues(queues, aircraftFromLoad, takeoffQueue, landingQueue, loadedMap);


        //creating a new control tower object and adding its terminals
        ControlTower controlTower =
                new ControlTower(loadedTicks, aircraftFromLoad,
                        landingQueue, takeoffQueue, loadedMap);
        for (Terminal terminal : terminalsFromLoad) {
            controlTower.addTerminal(terminal);
        }
        return controlTower;
    }

    /***
     * Reads an aircraft from its encoded representation in the given string.
     * @param line line of text containing the encoded aircraft
     * @return decoded aircraft instance
     * @throws MalformedSaveException if the format of the given string is invalid
     */
    public static Aircraft readAircraft(String line) throws MalformedSaveException {
        String[] aircraftSplit = line.split(":");
        double fuelAmount;
        int freightPassenger;

        //Setting ac as null by default
        AircraftCharacteristics ac = null;

        //Checking if the line is empty
        if (line.isEmpty()) {
            throw new MalformedSaveException();
        }

        //elements after splitting the string are 6,
        //length must be equal to 6
        if (aircraftSplit.length != 6) {
            throw new MalformedSaveException();
        }

        for (AircraftCharacteristics characteristic : AircraftCharacteristics.values()) {
            if (characteristic.name().equals(aircraftSplit[1])) {
                ac = characteristic;
            }
        }

        for (int i = 0; i < AircraftCharacteristics.values().length; i++) {
            if (AircraftCharacteristics.values()[i].name().equals(aircraftSplit[1])) {
                ac = AircraftCharacteristics.values()[i];
            }
        }

        if (ac == null) {
            throw new MalformedSaveException();
        }

        try {
            fuelAmount = Double.parseDouble(aircraftSplit[3]);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }

        //TASK LIST PART
        TaskList taskList = readTaskList(aircraftSplit[2]);
        try {
            freightPassenger = Integer.parseInt(aircraftSplit[5]);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }

        //returning a new aircraft object, depending on the value of
        // freight or cargo in the aircraft characteristics
        try {
            if (ac.passengerCapacity > 0) {
                return new PassengerAircraft(aircraftSplit[0], ac, taskList, fuelAmount,
                        freightPassenger);
            } else {
                return new FreightAircraft(aircraftSplit[0], ac, taskList, fuelAmount,
                        freightPassenger);
            }

        } catch (IllegalArgumentException illegalArgumentException) {
            throw new MalformedSaveException();
        }
    }


    /***
     * Reads a task list from its encoded representation in the given string.
     * @param taskListPart string containing the encoded task list
     * @return decoded task list instance
     * @throws MalformedSaveException if the format of the given string is invalid
     */
    public static TaskList readTaskList(String taskListPart) throws MalformedSaveException {
        if (taskListPart == null) {
            throw new MalformedSaveException();
        }

        String[] taskListString = taskListPart.split(",");

        //Setting ac as null by default
        TaskType taskType = null;
        int loadPercentValue;

        for (String task : taskListString) {
            //must be a valid task
            if (!((task.startsWith("LOAD")) || (task.contains("WAIT")) || (task.contains("LAND"))
                    || (task.contains("AWAY")) || (task.contains("TAKEOFF")))) {
                throw new MalformedSaveException();
            }
            // cannot start with @
            if (task.startsWith("@")) {
                throw new MalformedSaveException();
            }
            //if task is load, must contain @
            if ((task.contains("LOAD")) && !(task.contains("@"))) {
                throw new MalformedSaveException();
            }
            //if task is load, must contain loading %
            if ((task.startsWith("LOAD")) && (task.endsWith("@"))) {
                throw new MalformedSaveException();
            }
            if ((task.contains("LOAD@@"))) {
                throw new MalformedSaveException();
            }

        }

        for (String task : taskListString) {
            if (task.contains("LOAD")) {
                try {

                    loadPercentValue = Integer.parseInt(task.split("@")[1]);
                } catch (NumberFormatException numberFormatException) {
                    throw new MalformedSaveException();
                }

                if (loadPercentValue < 0) {
                    throw new MalformedSaveException();
                }
            }
        }

        //making a new task list, using switch to add tasks other than load
        // if load, split the @  and return it as a task.
        List<Task> loadTaskList = new LinkedList<>();
        for (String task : taskListString) {

            Task temporaryTask = null;
            if (task.startsWith("LOAD")) {
                loadPercentValue = Integer.parseInt(task.split("@")[1]);
                temporaryTask = new Task(TaskType.LOAD, loadPercentValue);
            }

            switch (task) {
                case "WAIT":
                    temporaryTask = new Task(TaskType.WAIT);
                    break;

                case "LAND":
                    temporaryTask = new Task(TaskType.LAND);
                    break;

                case "TAKEOFF":
                    temporaryTask = new Task(TaskType.TAKEOFF);
                    break;

                case "AWAY":
                    temporaryTask = new Task(TaskType.AWAY);
                    break;
            }
            loadTaskList.add(temporaryTask);
        }
        return new TaskList(loadTaskList);
    }

    /***
     * Reads an aircraft queue from the given reader instance.
     * @param reader reader from which to load the aircraft queue
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param queue empty queue that aircraft will be added to
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException  if the format of the text read from the reader is invalid
     */
    public static void readQueue(BufferedReader reader, List<Aircraft> aircraft, AircraftQueue
            queue) throws IOException, MalformedSaveException {
        String queuesLine = reader.readLine();
        String[] firstLine = queuesLine.split(":");

        //firstLine real from reader cant be null
        if (queuesLine.isEmpty()) {
            throw new MalformedSaveException();
        }

        //first line must contain exact amount of colons
        if (firstLine.length != 2) {
            throw new MalformedSaveException();
        }

        //simple class name must match queue type
        if ((!firstLine[0].equals("TakeoffQueue")) && ((!firstLine[0].equals("LandingQueue")))) {
            throw new MalformedSaveException();
        }


        int numAircraft;
        // number of aircraft specified on the first line must be an int
        try {
            numAircraft = Integer.parseInt(firstLine[1]);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }

        //number of aircraft specified cant be less than 0
        if (numAircraft < 0) {
            throw new MalformedSaveException();
        }

        // checking if numAircraft is greater than zero
        if (numAircraft > 0) {
            String secondLine = reader.readLine();
            // second line must not be null if numAircraft is greater than 0
            if (secondLine == null) {
                throw new MalformedSaveException();
            }

            //the number of callsigns listed on the second line is not
            String[] callsigns = secondLine.split(",");
            if (callsigns.length != numAircraft) {
                throw new MalformedSaveException();
            }

            //callsign does not correspond to another aircraft on the list
            for (Aircraft singleAircraft : aircraft) {
                for (int j = 0; j < callsigns.length; j++) {
                    if (aircraft.get(j).getCallsign().equals(singleAircraft.getCallsign())) {
                        queue.addAircraft(singleAircraft);
                    }
                }
            }
        }
    }

    /***
     * Reads the map of currently loading aircraft from the given reader instance.
     * @param reader reader from which to load the gates of the terminal(subsequent lines)
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param loadingAircraft  empty map that aircraft and their loading times will be added to
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is invalid
     */
    public static void readLoadingAircraft(BufferedReader reader, List<Aircraft> aircraft,
                                           Map<Aircraft, Integer> loadingAircraft) throws
            IOException, MalformedSaveException {
        String firstLine = reader.readLine();

        //first line read from reader cannot be null
        if (firstLine == null) {
            throw new MalformedSaveException();
        }
        //number of colons on first line must match what's expected
        String[] firstLineSplit = firstLine.split(":");
        if (firstLineSplit.length != 2) {
            throw new MalformedSaveException();
        }

        //number of aircraft on the first line must be an integer
        int numberOfAircraft;
        try {
            numberOfAircraft = Integer.parseInt(firstLineSplit[1]);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }

        String nextLine = reader.readLine();

        if (numberOfAircraft > 0 && nextLine == null) {
            throw new MalformedSaveException();
        }

        if (numberOfAircraft == 0) {
            return;
        }

        String[] secondLine = nextLine.split(",");
        int lengthOfSecondLine = secondLine.length;

        if (numberOfAircraft != lengthOfSecondLine) {
            throw new MalformedSaveException();
        }


        if (Integer.parseInt(firstLineSplit[1]) > 0) {

            String[] aircraftInfo;
            String callSign;
            String getLoadingTime;
            int ticksRemaining;
            boolean callsignMatch;

            //for any callsign / loading time pair on the second line must equal one
            for (String s : secondLine) {
                aircraftInfo = s.split(":");
                callSign = aircraftInfo[0];
                getLoadingTime = aircraftInfo[1];
                callsignMatch = false;
                for (Aircraft singleAircraft : aircraft) {
                    if (callSign.equals(singleAircraft.getCallsign())) {
                        loadingAircraft.put(singleAircraft, Integer.parseInt(getLoadingTime));
                        callsignMatch = true;
                        break;
                    }
                }
                if (!callsignMatch) {
                    throw new MalformedSaveException();
                }
                if (aircraftInfo.length != 2) {
                    throw new MalformedSaveException();
                }
                try {
                    ticksRemaining = Integer.parseInt(getLoadingTime);
                } catch (NumberFormatException numberFormatException) {
                    throw new MalformedSaveException();
                }
                if (ticksRemaining < 0) {
                    throw new MalformedSaveException();
                }
            }
        }
    }

    /***
     * Reads a terminal from the given string and reads its gates from the given reader instance.
     * @param line string containing the first line of the encoded terminal
     * @param reader reader from which to load the gates of the terminal (subsequent lines)
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded terminal with its gates added
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if an IOException is encountered when reading from the reader
     */
    public static Terminal readTerminal(String line, BufferedReader reader,
                                        List<Aircraft> aircraft) throws IOException,
            MalformedSaveException {
        List<Gate> loadedGates = new LinkedList<>();
        int terminalNumber;

        if (line == null) {
            throw new MalformedSaveException();
        }

        String[] lineTerminal = line.split(":");

        if (lineTerminal.length != 4) {
            throw new MalformedSaveException();
        }

        //terminal type must be instaceOf helicopterterminal or airplaneterminal
        if (!(lineTerminal[0].startsWith("AirplaneTerminal"))
                && !(lineTerminal[0].startsWith("HelicopterTerminal"))) {
            throw new MalformedSaveException();
        }

        //terminal number must parse as an int
        try {
            terminalNumber = Integer.parseInt(lineTerminal[1]);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }

        //terminal number cannot be less than one
        if (terminalNumber < 1) {
            throw new MalformedSaveException();
        }

        //number of gates in terminal must be an integer
        int totalGates;

        try {
            totalGates = Integer.parseInt(lineTerminal[3]);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }

        //Gates must be between 0 and 6 inclusive
        if (totalGates < 0) {
            throw new MalformedSaveException();
        }
        if (totalGates > Terminal.MAX_NUM_GATES) {
            throw new MalformedSaveException();
        }



        for (int i = 0; i < totalGates; i++) {
            String gatesLine = reader.readLine();
            if (gatesLine == null) {
                throw new MalformedSaveException();
            }
            loadedGates.add(readGate(gatesLine, aircraft));
        }

        if (lineTerminal[0].startsWith("Helicopter")) {
            AirplaneTerminal terminal = new AirplaneTerminal(terminalNumber);
            if (lineTerminal[2].equals("true")) {
                terminal.declareEmergency();
            }
            for (Gate gate : loadedGates) {
                try {
                    terminal.addGate(gate);
                } catch (NoSpaceException noSpaceException) {
                    throw new MalformedSaveException("Gate Setup Failed:", noSpaceException);
                }
            }
            return terminal;

            //trying to create a new airplane terminal object
        } else {
            HelicopterTerminal terminal = new HelicopterTerminal(terminalNumber);
            if (lineTerminal[2].equals("true")) {
                terminal.declareEmergency();
            }
            for (Gate gate : loadedGates) {
                try {
                    terminal.addGate(gate);
                } catch (NoSpaceException noSpaceException) {
                    throw new MalformedSaveException("Gate Setup Failed:", noSpaceException);
                }
            }
            return terminal;
        }
    }

    /***
     * Reads a gate from its encoded representation in the given string.
     * @param line string containing the encoded gate
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded gate instance
     * @throws MalformedSaveException  if the format of the given string is invalid
     */
    public static Gate readGate(String line, List<Aircraft> aircraft)
            throws MalformedSaveException {
        int gateNumber;
        String[] gateLine = line.split(":");

        if (gateLine.length != 2) {
            throw new MalformedSaveException();
        }

        // gate number must be an integer
        try {
            gateNumber = Integer.parseInt(gateLine[0]);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }

        // gate number must be 1
        if (gateNumber < 1) {
            throw new MalformedSaveException();
        }

        // trying to create a new gate object
        Gate gate = new Gate(gateNumber);

        // looping through
        for (Aircraft singleAircraft : aircraft) {
            if (!(gateLine[1].equals("empty"))) {
                try {
                    if (singleAircraft.getCallsign().equals(gateLine[1])) {
                        gate.parkAircraft(singleAircraft);
                        return gate;
                    }
                } catch (NoSpaceException noSpaceException) {
                    throw new MalformedSaveException();
                }
            }
        }
        return  gate;
    }
}