package zerobase.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDateException;
import zerobase.weather.repository.DateWeatherRepository;
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
@Transactional(readOnly = true)
@Slf4j
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    @Value("${openweathermap.key}")
    private String apiKey;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherDataFromApi());
        log.info("getWeatherDataFromApi SUCCESS");
    }

    @Transactional
    public void createDiary(LocalDate date, String text) {

        DateWeather dateWeather = getDateWeather(date);

        Diary diary = Diary.builder()
                .date(date)
                .text(text)
                .icon(dateWeather.getIcon())
                .temperature(dateWeather.getTemperature())
                .weather(dateWeather.getWeather())
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

    //????????? mainData??? ???????????? ????????? temp?????? ?????????
    //????????? weatherData??? ???????????? main, icon??? ?????????
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
//        if (date.isAfter(LocalDate.ofYearDay(3050, 1))) {
//            throw new InvalidDateException();
//        }
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    @Transactional
    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.findFirstByDate(date);

        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
    }

    @Transactional
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }

    private DateWeather getWeatherDataFromApi() {
        String jsonString = getWeatherString();
        Map<String, Object> parsedWeather = parseWeather(jsonString);

        DateWeather dateWeather = DateWeather.builder()
                .date(LocalDate.now())
                .weather((String) parsedWeather.get("main"))
                .icon((String) parsedWeather.get("icon"))
                .temperature((Double) parsedWeather.get("temp"))
                .build();

        return dateWeather;
    }

    private DateWeather getDateWeather(LocalDate date) {

        List<DateWeather> dateWeatherList = dateWeatherRepository.findAllByDate(date);

        if (dateWeatherList.size() == 0) {
            //?????? ????????? ????????? ????????? ??? ?????? ?????????
            //?????? ???????????? ?????? ????????? ???????????? ??????.
            return getWeatherDataFromApi();
        } else {
            return dateWeatherList.get(0);
        }

    }
}
