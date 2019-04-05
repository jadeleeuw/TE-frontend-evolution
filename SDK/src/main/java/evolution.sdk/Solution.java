package evolution.sdk;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import sdk.railways.ApiClient;
import sdk.railways.api.DistanceApi;
import sdk.railways.api.RailwayApi;
import sdk.railways.api.StationApi;
import sdk.railways.api.TaskApi;
import sdk.railways.model.Route;
import sdk.railways.model.Task;

import java.util.*;
import java.util.stream.Collectors;

public class Solution {

    private StationApi stationApi;
    private RailwayApi railwayApi;
    private TaskApi taskApi;
    private DistanceApi distanceApi;

    public Solution() {
        ApiClient client = new ApiClient();
        client.setApiKey(String.valueOf(961270782));

        this.stationApi = new StationApi(client);
        this.railwayApi = new RailwayApi(client);
        this.taskApi = new TaskApi(client);
        this.distanceApi = new DistanceApi(client);
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
            stationApi.getRailwaysConnectedToStation(currentStation)
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
        new Solution().solve();
    }
}