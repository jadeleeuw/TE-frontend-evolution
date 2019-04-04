package evolution.nsdk.api;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import evolution.nsdk.models.Route;
import evolution.nsdk.models.Task;
import reactor.core.publisher.Mono;

public class TaskAPI extends API {

    public TaskAPI(int idKey) {
        super(idKey);
    }

    public Mono<Task> getTask() {
        try {
            return Mono.just(
                     Unirest.get(baseURL + "/task")
                    .header("x-id-key", String.valueOf(idKey))
                    .asObject(Task.class)
                    .getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }

    public Mono<String> submit(Route route) {
        try {
            return Mono.just(
                     Unirest.post(baseURL + "/task/submit")
                    .header("x-id-key", String.valueOf(idKey))
                    .header("Content-Type", "application/json")
                    .body(route)
                    .asString()
                    .getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }
}


