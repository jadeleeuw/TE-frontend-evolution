package evolution.nsdk.api;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import reactor.core.publisher.Flux;

public class RailwayAPI extends API {

    public RailwayAPI(int idKey) {
        super(idKey);
    }

    public Flux<Integer> getStationsOnRailway(int railwayId) {
        try {
            return Flux.fromArray(
                     Unirest.get(baseURL + "/railways/{railwayId}/stations")
                    .header("x-id-key", String.valueOf(idKey))
                    .routeParam("railwayId", String.valueOf(railwayId))
                    .asObject(Integer[].class)
                    .getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return Flux.empty();
    }
}
