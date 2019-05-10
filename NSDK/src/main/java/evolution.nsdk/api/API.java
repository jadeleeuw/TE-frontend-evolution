package evolution.nsdk.api;

public abstract class API {

    protected final String baseURL = "https://jeansthesis.nl:8080";
    protected final int idKey;

    public API(int idKey) {
        this.idKey = idKey;
    }
}
