import java.io.IOException;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import com.google.gson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {
    public static final String WEATHER_URL = "https://api.weather.yandex.ru/v2/forecast?lat=52.37125&lon=4.89388&limit=3";
    public static final String WEATHER_API_HEADER_KEY = "X-Yandex-API-Key";

    public static void main(String[] args) {
        var weatherKey = getWeatherServiceKey();
        if (weatherKey == null || weatherKey.isEmpty()) {
            System.out.println("error: please set WEATHER_KEY environment variable");
            return;
        }

        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WEATHER_URL))
                    .GET()
                    .header(WEATHER_API_HEADER_KEY, weatherKey)
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();

                printPrettyJson(responseBody);
                printTemperature(responseBody);
                printAvgTemperature(responseBody);

            } catch (Exception e) {
                System.err.println("Error making HTTP request: " + e.getMessage());
            }
        }
    }

    private static String getWeatherServiceKey() {
        return  System.getenv("WEATHER_KEY");
    }

    private static void printPrettyJson(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(json);
        String prettyJsonString = gson.toJson(je);
        System.out.println(prettyJsonString);
    }

    public static void printTemperature(String json) throws ParseException {
        Object obj = new JSONParser().parse(json);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject fact = (JSONObject) jsonObject.get("fact");
        Long temp = (Long) fact.get("temp");
        System.out.println("Температура: " + temp);
    }

    public static void printAvgTemperature(String json) throws ParseException {
        Long tempAvgSum = 0L;
        Object obj = new JSONParser().parse(json);
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray forecasts = (JSONArray) jsonObject.get("forecasts");
        for (Object o : forecasts) {
            JSONObject forecast = (JSONObject) o;
            JSONObject parts = (JSONObject) forecast.get("parts");
            JSONObject day = (JSONObject) parts.get("day");
            Long tempAvg = (Long) day.get("temp_avg");
            tempAvgSum += tempAvg;
        }
        System.out.println("Среднее значение температуры: " + tempAvgSum/forecasts.size());
    }
}