package zerobase.weather.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@ApiResponses({
        @ApiResponse(code = 500, message = "서버 에러")
})
public class DiaryController {

    private final DiaryService diaryService;

    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장")
    @PostMapping(value = "/create/diary")
    void createDiary(
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "일기를 쓸 날짜", example = "2020-03-10")
            LocalDate date,
            @RequestBody String text) {

        diaryService.createDiary(date, text);
    }

    @ApiOperation(value = "선택한 날짜의 모든 일기 데이터를 가져옵니다.")
    @GetMapping(value = "/read/diary")
    List<Diary> readDiary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "날짜 형식 : yyyy -MM -dd", example = "2020-03-10") LocalDate date
    ) {
        return diaryService.readDiary(date);
    }
    @ApiOperation(value = "선택한 날짜의 기간 중의 모든 일기 데이터를 가져옵니다.")
    @GetMapping(value = "/read/diaries")
    List<Diary> readDiaries(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회할 기간의 첫번째날", example = "2020-03-10")
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회할 기간의 마지막날", example = "2020-08-30")
            LocalDate endDate
    ) {
        return diaryService.readDiaries(startDate, endDate);
    }

    @ApiOperation(value = "선택한 날짜의 첫번째 일기의 내용을 변경합니다.")
    @PostMapping(value = "/update/diary")
    void updateDiary(
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "변경할 날짜", example = "2020-03-10")
            LocalDate date,
            @RequestBody String text
    ) {
        diaryService.updateDiary(date, text);
    }

    @ApiOperation(value = "선택한 날짜의 모든 일기 데이터를 지웁니다.")
    @DeleteMapping(value = "/delete/diary")
    void deleteDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "일기를 지울 날짜", example = "2020-03-10")
            LocalDate date
    ) {
        diaryService.deleteDiary(date);
    }
}
