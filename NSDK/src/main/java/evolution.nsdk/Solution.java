package evolution.nsdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import evolution.nsdk.api.DistanceAPI;
import evolution.nsdk.api.RailwayAPI;
import evolution.nsdk.api.StationAPI;
import evolution.nsdk.api.TaskAPI;
import evolution.nsdk.models.Route;
import evolution.nsdk.models.Task;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Solution {

    private TaskAPI taskApi;
    private StationAPI stationApi;
    private RailwayAPI railwayApi;
    private DistanceAPI distanceApi;

    public Solution() {
        taskApi = new TaskAPI(961270782);
        stationApi = new StationAPI(961270782);
        railwayApi = new RailwayAPI(961270782);
        distanceApi = new DistanceAPI(961270782);
    }


    public List<Route> explore(int originStation) {
        //Stream generator of possible routes
        ArrayList<List<Integer>> toExplore = new ArrayList<>();
        toExplore.add(Collections.singletonList(originStation));
        Flux<List<Integer>> explorationQueue = Flux.generate(sink -> {
            if(toExplore.size() == 0) {
                sink.complete();
            } else {
                sink.next(toExplore.remove(0));
            }
        });

        List<Integer> visitedRailways = new ArrayList<>();
        //Explore all possible routes
        return explorationQueue.doOnNext(partialRoute -> {
            int currentStation = partialRoute.get(partialRoute.size() - 1);
            stationApi.getRailwaysOnStation(currentStation)
                    .filter(railwayId -> !visitedRailways.contains(railwayId))
                    .doOnNext(visitedRailways::add)

                    .flatMap(railwayApi::getStationsOnRailway)
                    .filter(nextStation -> currentStation != nextStation)

                    .doOnNext(nextStation -> {
                        List<Integer> newRoute = new ArrayList<>(partialRoute);
                        newRoute.add(nextStation);
                        toExplore.add(newRoute);
                        System.out.println("Routes to explore: " + toExplore.size());
                    }).blockLast();
        }).map(completeRoute -> {       // Transform to Route objects.
            Route route = new Route();
            route.setStationsToSwitchTrain(completeRoute);
            return route;
        }).collectList().block();
    }

    public List<Route> filter(List<Route> routes, int originStation, int destinationStation) {
        return routes.stream()
                .filter(route -> route.getStationsToSwitchTrain().get(0) == originStation)
                .filter(route -> route.getStationsToSwitchTrain().get(route.getStationsToSwitchTrain().size() - 1) == destinationStation)
                .collect(Collectors.toList());
    }

    public Tuple2<Route, Double> findShortestPath(List<Route> routes) {
        return Flux.fromIterable(routes)
                .map(route -> Tuples.of(route,
                        distanceApi.getDistanceOfRoute(route)
                                .block()
                                .getDistance()))
                .reduce(Tuples.of(new Route(), Double.MAX_VALUE),
                        (a, b) -> b.getT2() < a.getT2() ? b : a)
                .block();
    }

    public void solve() {
        Task task = taskApi.getTask().block();

        List<Route> routes = explore(task.getOrigin()); //Explore all possible routes
        routes = filter(routes, task.getOrigin(), task.getDestination()); //Filter routes by validity
        Tuple2<Route, Double> shortestPath = findShortestPath(routes); //Find the shortest path

        System.out.println("Response: " + taskApi.submit(shortestPath.getT1()).block());

        System.out.println("Shortest path by station id: " + shortestPath.getT1().getStationsToSwitchTrain());
        System.out.println("Shortest path by station name: " + shortestPath.getT1().getStationsToSwitchTrain().stream().map(stationApi::getStationName).map(r -> r.block().getName()).collect(Collectors.toList()));
        System.out.println("Distance: " + shortestPath.getT2() + " meters.");
    }
    
    public static void main(String[] args) {
        Solution s = new Solution();
        s.unirestSerializerSetup();
        s.solve();
    }
    
    // This initializes Unirest, making (de)serialization of objects possible. You don't have to touch this.
    private void unirestSerializerSetup() {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
