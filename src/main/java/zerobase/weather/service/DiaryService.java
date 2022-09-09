package zerobase.weather.service;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    @Value("${openweathermap.key}")
    private String apiKey;
    public void createDiary(LocalDate date, String text) {

        Map<String, Object> jsonMap = parseWeather(getWeatherString());

        Diary diary = Diary.builder()
                .date(date)
                .text(text)
                .icon((String) jsonMap.get("icon"))
                .temperature((Double) jsonMap.get("temp"))
                .weather((String) jsonMap.get("main"))
                .build();

        diaryRepository.save(diary);
    }

    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=Seoul&appid=" + apiKey;
        try {
            URL url = new URL(apiUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader br;

            if (connection.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String inputLine = "";

            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();

        } catch (Exception e) {
            return "failed to get response";
        }
    }

    //형태가 mainData를 받아와서 여기서 temp값을 가져옴
    //형태가 weatherData를 받아와서 main, icon을 가져옴
    private Map<String, Object> parseWeather(String jsonString) {

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);

            JSONArray jsonArray = (JSONArray)jsonObject.get("weather");

            JSONObject weatherData = (JSONObject) jsonArray.get(0);
            JSONObject mainData = (JSONObject) jsonObject.get("main");

            Map<String, Object> jsonMap = new HashMap<>();

            jsonMap.put("main", weatherData.get("main"));
            jsonMap.put("icon", weatherData.get("icon"));
            jsonMap.put("temp", mainData.get("temp"));

            return jsonMap;

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Diary> readDiary(LocalDate date) {
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.findFirstByDate(date);

        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
    }

    @Transactional
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
