package evolution.nsdk.models;

import java.util.ArrayList;
import java.util.List;

public class Route {

    private List<Integer> stationsToSwitchTrain;

    public Route() {
        stationsToSwitchTrain = new ArrayList<>();
    }

    public List<Integer> getStationsToSwitchTrain() {
        return stationsToSwitchTrain;
    }

    public void setStationsToSwitchTrain(List<Integer> stationsToSwitchTrains) {
        this.stationsToSwitchTrain = stationsToSwitchTrains;
    }
}
