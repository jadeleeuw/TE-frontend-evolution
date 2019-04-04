package evolution.nsdk.api;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import evolution.nsdk.models.NameResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class StationAPI extends API {

    public StationAPI(int idKey) {
        super(idKey);
    }

    public Flux<Integer> getRailwaysOnStation(int stationId) {
        try {
            return Flux.fromArray(
                     Unirest.get(baseURL + "/stations/{stationId}/railways")
                    .header("x-id-key", String.valueOf(idKey))
                    .routeParam("stationId", String.valueOf(stationId))
                    .asObject(Integer[].class)
                    .getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return Flux.empty();
    }

    public Mono<NameResponse> getStationName(int stationId) {
        try {
            return Mono.just(
                     Unirest.get(baseURL + "/stations/{stationId}/name")
                    .header("x-id-key", String.valueOf(idKey))
                    .routeParam("stationId", String.valueOf(stationId))
                    .asObject(NameResponse.class)
                    .getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }
}
