package zerobase.weather;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anything;

@SpringBootTest
@Transactional
class WeatherApplicationTests {

    private final DiaryService diaryService;

    WeatherApplicationTests(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @Test
    void readDiaryTest() {
        assertThat(diaryService.readDiary(LocalDate.of(2022,9,7)).size(), CoreMatchers.is(2));
    }

}
