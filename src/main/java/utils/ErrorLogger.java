package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 애플리케이션 에러 로깅을 위한 유틸리티 클래스
 * 에러 메시지와 스택 트레이스를 로그 파일에 기록합니다.
 */
public class ErrorLogger {
    // 로그 파일 경로
    private static final String LOG_DIRECTORY = System.getProperty("user.home") + "/musicmood_logs";
    private static final String LOG_FILE = LOG_DIRECTORY + "/error_log.txt";
    
    // 날짜 포맷
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 에러 로그를 파일에 기록합니다.
     * 
     * @param errorMessage 에러 메시지
     * @param exception 발생한 예외 객체
     */
    public static void logError(String errorMessage, Exception exception) {
        // 로그 디렉토리 확인 및 생성
        createLogDirectory();
        
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(formatter);
        
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            pw.println("========== ERROR LOG ==========");
            pw.println("Timestamp: " + timestamp);
            pw.println("Error Message: " + errorMessage);
            pw.println("Exception: " + exception.getClass().getName());
            pw.println("Message: " + exception.getMessage());
            pw.println("Stack Trace:");
            
            // 스택 트레이스 기록
            for (StackTraceElement element : exception.getStackTrace()) {
                pw.println("\tat " + element.toString());
            }
            
            pw.println("==============================\n");
            
            // 표준 에러에도 간략한 로그 출력
            System.err.println("[" + timestamp + "] ERROR: " + errorMessage + " - " + exception.getMessage());
            
        } catch (IOException e) {
            // 로깅 실패 시 콘솔에 출력
            System.err.println("로그 파일 쓰기 실패: " + e.getMessage());
            System.err.println("원본 에러: " + errorMessage + " - " + exception.getMessage());
        }
    }
    
    /**
     * 에러 로그를 파일에 기록합니다. (메시지만 있는 경우)
     * 
     * @param errorMessage 에러 메시지
     */
    public static void logError(String errorMessage) {
        // 로그 디렉토리 확인 및 생성
        createLogDirectory();
        
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(formatter);
        
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            pw.println("========== ERROR LOG ==========");
            pw.println("Timestamp: " + timestamp);
            pw.println("Error Message: " + errorMessage);
            pw.println("==============================\n");
            
            // 표준 에러에도 간략한 로그 출력
            System.err.println("[" + timestamp + "] ERROR: " + errorMessage);
            
        } catch (IOException e) {
            // 로깅 실패 시 콘솔에 출력
            System.err.println("로그 파일 쓰기 실패: " + e.getMessage());
            System.err.println("원본 에러: " + errorMessage);
        }
    }
    
    /**
     * 로그 디렉토리를 생성합니다.
     */
    private static void createLogDirectory() {
        File directory = new File(LOG_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
} 