import core.Line;
import core.Station;
import junit.framework.TestCase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RouteCalculatorTest extends TestCase {

    List<Station> route;
    private static StationIndex stationIndexForTest;
    private static String dataFile = "src/main/resources/testMap.json";
    Station fromTestWithTwoConnections;
    Station toTestWithTwoConnections;
    Station fromTestWithOneConnections;
    Station toTestWithOneConnections;
    Station fromTestOnTheLine;
    Station toTestOnTheLine;
    RouteCalculator calculator;
    List<Station> expectedTestWithTwoConnections;
    List<Station> expectedTestWithOneConnections;
    List<Station> expectedTestOnTheLine;

    @Override
    protected void setUp() throws Exception {
        try {
            createStationIndex();
            calculator = new RouteCalculator(stationIndexForTest);

            /////////////////////////////// testCalculateDuration

            route = getList("Красная", "Бордовая", "Алая", "Синяя", "Голубая", "Серая", "Черная");

            /////////////////////////////// testGetRouteOnTheLine

            fromTestOnTheLine = stationIndexForTest.getStation("Красная", 1);
            toTestOnTheLine = stationIndexForTest.getStation("Алая", 1);

            expectedTestOnTheLine = getList("Красная", "Бордовая", "Алая");

            /////////////////////////////// testGetRouteWithOneConnections

            fromTestWithOneConnections = stationIndexForTest.getStation("Голубая", 2);
            toTestWithOneConnections = stationIndexForTest.getStation("Черная", 3);

            expectedTestWithOneConnections = getList("Голубая", "Серая", "Черная");

            /////////////////////////////// testGetRouteWithTwoConnections

            fromTestWithTwoConnections = stationIndexForTest.getStation("Бордовая", 1);
            toTestWithTwoConnections = stationIndexForTest.getStation("Серая", 3);

            expectedTestWithTwoConnections = getList("Бордовая", "Алая", "Синяя", "Голубая", "Серая");
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void testCalculateDuration(){
        double actual = RouteCalculator.calculateDuration(route);
        double expected = 17.0;
        assertEquals(expected, actual);
    }

    public void testGetRouteOnTheLine(){

        List<Station> actual = calculator.getShortestRoute(fromTestOnTheLine, toTestOnTheLine);
        List<Station> expected = new ArrayList<>(expectedTestOnTheLine);

        assertEquals(expected, actual);
    }

    public void testGetRouteWithOneConnections(){

        List<Station> actual = calculator.getShortestRoute(fromTestWithOneConnections, toTestWithOneConnections);
        List<Station> expected = new ArrayList<>(expectedTestWithOneConnections);

        assertEquals(expected, actual);
    }

    public void testGetRouteWithTwoConnections(){

        List<Station> actual = calculator.getShortestRoute(fromTestWithTwoConnections, toTestWithTwoConnections);
        List<Station> expected = new ArrayList<>(expectedTestWithTwoConnections);

          assertEquals(expected, actual);
    }

    @Override
    protected void tearDown() throws Exception {

    }


    private static void createStationIndex()
    {
        stationIndexForTest = new StationIndex();
        try
        {
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject) parser.parse(getJsonFile());

            JSONArray linesArray = (JSONArray) jsonData.get("lines");
            parseLines(linesArray);

            JSONObject stationsObject = (JSONObject) jsonData.get("stations");
            parseStations(stationsObject);

            JSONArray connectionsArray = (JSONArray) jsonData.get("connections");
            parseConnections(connectionsArray);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void parseConnections(JSONArray connectionsArray)
    {
        connectionsArray.forEach(connectionObject ->
        {
            JSONArray connection = (JSONArray) connectionObject;
            List<Station> connectionStations = new ArrayList<>();
            connection.forEach(item ->
            {
                JSONObject itemObject = (JSONObject) item;
                int lineNumber = ((Long) itemObject.get("line")).intValue();
                String stationName = (String) itemObject.get("station");

                Station station = stationIndexForTest.getStation(stationName, lineNumber);
                if(station == null)
                {
                    throw new IllegalArgumentException("core.Station " +
                            stationName + " on line " + lineNumber + " not found");
                }
                connectionStations.add(station);
            });
            stationIndexForTest.addConnection(connectionStations);
        });
    }

    private static void parseStations(JSONObject stationsObject)
    {
        stationsObject.keySet().forEach(lineNumberObject ->
        {
            int lineNumber = Integer.parseInt((String) lineNumberObject);
            Line line = stationIndexForTest.getLine(lineNumber);
            JSONArray stationsArray = (JSONArray) stationsObject.get(lineNumberObject);
            stationsArray.forEach(stationObject ->
            {
                Station station = new Station((String) stationObject, line);
                stationIndexForTest.addStation(station);
                line.addStation(station);
            });
        });
    }

    private static void parseLines(JSONArray linesArray)
    {
        linesArray.forEach(lineObject -> {
            JSONObject lineJsonObject = (JSONObject) lineObject;
            Line line = new Line(
                    ((Long) lineJsonObject.get("number")).intValue(),
                    (String) lineJsonObject.get("name")
            );
            stationIndexForTest.addLine(line);
        });
    }

    private static String getJsonFile()
    {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(dataFile));
            lines.forEach(line -> builder.append(line));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }

    public List<Station> getList(String... names)
    {
        ArrayList<Station> stationList = new ArrayList<>();
        for(String name : names){
            stationList.add(stationIndexForTest.getStation(name));
        }
        return stationList;
    }
}