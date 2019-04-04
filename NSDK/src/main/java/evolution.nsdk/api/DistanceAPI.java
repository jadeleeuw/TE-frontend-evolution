package evolution.nsdk.api;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import evolution.nsdk.models.DistanceResponse;
import evolution.nsdk.models.Route;
import reactor.core.publisher.Mono;


public class DistanceAPI extends API {

    public DistanceAPI(int idKey) {
        super(idKey);
    }

    public Mono<DistanceResponse> getDistanceOfRoute(Route route) {
        try {
            return Mono.just(
                     Unirest.post(baseURL + "/distance/route")
                    .header("x-id-key", String.valueOf(idKey))
                    .header("Content-Type", "application/json")
                    .body(route)
                    .asObject(DistanceResponse.class)
                    .getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return Mono.empty();
    }
}
