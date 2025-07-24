package listeners;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.PrintWriter;
import java.util.logging.Logger;

import controller.*;
import controller.Controller;
import dao.*;
import utils.ErrorLogger;

/**
 * 애플리케이션 컨텍스트 로더
 * 웹 애플리케이션 시작/종료 시 필요한 초기화와 정리 작업을 수행합니다.
 * 
 * 주요 기능:
 * 1. 데이터베이스 연결 초기화 및 관리
 * 2. 컨트롤러, DAO 객체 생성 및 의존성 주입
 * 3. 애플리케이션 전역 설정/자원 관리
 */
public class ContextLoaderListener implements ServletContextListener {

    // 애플리케이션 컨텍스트는 애플리케이션 전체에 걸쳐 공유되는 정보를 저장하는 객체임.
    // 예를 들어 애플리케이션 전체에 걸쳐 사용되는 데이터베이스 연결 정보, 설정 정보 등이 저장됨.
    private static Map<String, Object> applicationContext = new HashMap<>();
    
    /**
     * Dispatcher 서블릿에서 호출하는 정적 메서드
     * ServletContextEvent 없이 초기화 작업을 수행합니다.
     */
    public static void contextInitialized() {
        System.out.println("====================================================");
        System.out.println("=== MusicMood 애플리케이션 시작 - 정적 초기화 작업 시작 ===");
        System.out.println("====================================================");
        
        try {
            // 데이터소스 초기화
            // 초기화를 하는 이유는 데이터베이스 연결 정보를 저장하기 위함임.
            initializeDataSourceStatic();
            
            // 애플리케이션 컨텍스트 객체 생성
            // 애플리케이션 컨텍스트 객체 생성을 하는 이유는 컨트롤러, DAO 객체를 생성하기 위함임.
            createApplicationObjectsStatic();
            
            System.out.println("=== 정적 초기화 작업 완료 - 애플리케이션 준비됨 ===");
            
            // 최종 DB 연결 테스트
            testDatabaseConnectionStatic();
            
        } catch (Throwable e) {
            System.err.println("[심각한 오류] 애플리케이션 정적 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Dispatcher 서블릿에서 호출하는 정적 정리 메서드
     */
    public static void contextDestroyed() {
        System.out.println("=== MusicMood 애플리케이션 종료 - 정적 정리 작업 수행 ===");
        // 필요한 정리 작업 수행
    }
    
    /**
     * 요청 경로에 해당하는 컨트롤러를 반환하는 정적 메서드
     * 
     * @param path 요청 경로 (예: "/login.do")
     * @return 요청 경로에 해당하는 컨트롤러 객체, 없으면 null
     */
    public static Controller getController(String path) {
        return (Controller) applicationContext.get(path);
    }
    
    /**
     * 애플리케이션 시작 시 호출되는 메서드
     * 필요한 모든 초기화 작업 수행
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("====================================================");
        System.out.println("=== MusicMood 애플리케이션 시작 - 초기화 작업 시작 ===");
        System.out.println("====================================================");
        
        ServletContext sc = event.getServletContext();
        
        try {
            // JDBC 드라이버 로드
            String jdbcDriver = sc.getInitParameter("jdbcDriver");
            if (jdbcDriver != null && !jdbcDriver.isEmpty()) {
                System.out.println("[초기화] JDBC 드라이버 로드: " + jdbcDriver);
                Class.forName(jdbcDriver);
            }
            
            // 데이터소스 초기화
            initializeDataSource();
            
            // 애플리케이션 컨텍스트 객체 생성
            createApplicationObjects();
            
            // 서블릿 컨텍스트에 애플리케이션 컨텍스트 저장
            sc.setAttribute("applicationContext", applicationContext);
            
            System.out.println("=== 초기화 작업 완료 - 애플리케이션 준비됨 ===");
            
            // 최종 DB 연결 테스트
            testDatabaseConnection();
            
        } catch (Throwable e) {
            System.err.println("[심각한 오류] 애플리케이션 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 애플리케이션 종료 시 호출되는 메서드
     * 자원 정리 작업 수행
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("=== MusicMood 애플리케이션 종료 - 정리 작업 수행 ===");
    }
    
    /**
     * 데이터소스 초기화 메서드
     * JNDI 또는 커스텀 데이터소스 설정
     */
    private void initializeDataSource() {
        System.out.println("[초기화] 데이터소스 초기화 시작");
        
        try {
            // JNDI 설정을 통한 데이터소스 획득 시도
            DataSource ds = getJndiDataSource();
            
            // JNDI 실패 시 SimpleDataSource 사용
            if (ds == null) {
                System.out.println("[초기화] JNDI 데이터소스 획득 실패, SimpleDataSource 사용");
                ds = new SimpleDataSource();
            }
            
            // 애플리케이션 컨텍스트에 데이터소스 저장
            applicationContext.put("dataSource", ds);
            
            System.out.println("[초기화] 데이터소스 초기화 완료");
            
        } catch (Exception e) {
            System.err.println("[심각한 오류] 데이터소스 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("데이터소스 초기화 실패", e);
        }
    }
    
    /**
     * 데이터소스 초기화 메서드 (정적 버전)
     */
    private static void initializeDataSourceStatic() {
        System.out.println("[초기화] 데이터소스 정적 초기화 시작");
        
        try {
            // JNDI 설정을 통한 데이터소스 획득 시도
            DataSource ds = getJndiDataSourceStatic();
            
            // JNDI 실패 시 SimpleDataSource 사용
            if (ds == null) {
                System.out.println("[초기화] JNDI 데이터소스 획득 실패, SimpleDataSource 사용");
                ds = new SimpleDataSource();
            }
            
            // 애플리케이션 컨텍스트에 데이터소스 저장
            applicationContext.put("dataSource", ds);
            
            System.out.println("[초기화] 데이터소스 정적 초기화 완료");
            
        } catch (Exception e) {
            System.err.println("[심각한 오류] 데이터소스 정적 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("데이터소스 정적 초기화 실패", e);
        }
    }
    
    /**
     * JNDI를 통한 데이터소스 획득 시도
     */
    private DataSource getJndiDataSource() {
        try {
            System.out.println("[초기화] JNDI 데이터소스 획득 시도");
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/musicdb");
            System.out.println("[초기화] JNDI 데이터소스 획득 성공");
            return ds;
        } catch (Exception e) {
            System.err.println("[경고] JNDI 데이터소스 획득 실패: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * JNDI를 통한 데이터소스 획득 시도 (정적 버전)
     */
    private static DataSource getJndiDataSourceStatic() {
        try {
            System.out.println("[초기화] JNDI 데이터소스 획득 시도 (정적)");
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/musicdb");
            System.out.println("[초기화] JNDI 데이터소스 획득 성공 (정적)");
            return ds;
        } catch (Exception e) {
            System.err.println("[경고] JNDI 데이터소스 획득 실패 (정적): " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 최종 데이터베이스 연결 테스트
     */
    private void testDatabaseConnection() {
        System.out.println("[초기화] 최종 데이터베이스 연결 테스트");
        DataSource ds = (DataSource) applicationContext.get("dataSource");
        
        if (ds == null) {
            System.err.println("[심각한 오류] 데이터소스가 초기화되지 않았습니다.");
            return;
        }
        
        Connection conn = null;
        try {
            conn = ds.getConnection();
            System.out.println("[초기화] 데이터베이스 연결 성공");
            
            // 데이터베이스 정보 출력
            System.out.println("[DB 정보] URL: " + conn.getMetaData().getURL());
            System.out.println("[DB 정보] 드라이버: " + conn.getMetaData().getDriverName());
            System.out.println("[DB 정보] 데이터베이스 제품: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("[DB 정보] 데이터베이스 버전: " + conn.getMetaData().getDatabaseProductVersion());
            
        } catch (SQLException e) {
            System.err.println("[심각한 오류] 데이터베이스 연결 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("최종 데이터베이스 연결 테스트 실패", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    ErrorLogger.logError("데이터베이스 연결 종료 실패", e);
                }
            }
        }
    }
    
    /**
     * 최종 데이터베이스 연결 테스트 (정적 버전)
     */
    private static void testDatabaseConnectionStatic() {
        System.out.println("[초기화] 최종 데이터베이스 연결 테스트 (정적)");
        DataSource ds = (DataSource) applicationContext.get("dataSource");
        
        if (ds == null) {
            System.err.println("[심각한 오류] 데이터소스가 초기화되지 않았습니다 (정적).");
            return;
        }
        
        Connection conn = null;
        try {
            conn = ds.getConnection();
            System.out.println("[초기화] 데이터베이스 연결 성공 (정적)");
            
            // 데이터베이스 정보 출력
            System.out.println("[DB 정보] URL: " + conn.getMetaData().getURL());
            System.out.println("[DB 정보] 드라이버: " + conn.getMetaData().getDriverName());
            System.out.println("[DB 정보] 데이터베이스 제품: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("[DB 정보] 데이터베이스 버전: " + conn.getMetaData().getDatabaseProductVersion());
            
        } catch (SQLException e) {
            System.err.println("[심각한 오류] 데이터베이스 연결 테스트 실패 (정적): " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("최종 데이터베이스 연결 테스트 실패 (정적)", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    ErrorLogger.logError("데이터베이스 연결 종료 실패 (정적)", e);
                }
            }
        }
    }
    
    /**
     * 애플리케이션에서 사용할 객체들을 생성하고 의존성을 주입하는 메서드
     */
    private void createApplicationObjects() {
        System.out.println("[초기화] 애플리케이션 객체 생성 시작");
        
        try {
            // 데이터소스 가져오기
            DataSource ds = (DataSource) applicationContext.get("dataSource");
            
            if (ds == null) {
                throw new Exception("데이터소스가 초기화되지 않았습니다.");
            }
            
            // DAO 객체 생성
            System.out.println("[초기화] DAO 객체 생성");
            UserDAO userDAO = new UserDAO(ds);
            PlaylistDAO playlistDAO = new PlaylistDAO(ds);
            // ... 다른 DAO 객체들 ...
            
            
            // 컨트롤러 객체 생성 및 DAO 의존성 주입
            System.out.println("[초기화] 컨트롤러 객체 생성");
            applicationContext.put("/index.do", new IndexController());
            applicationContext.put("/auth/login.do", new AuthController(userDAO));
            applicationContext.put("/signup.do", new SignupController(userDAO));
            applicationContext.put("/search.do", new SearchController());
            applicationContext.put("/token.do", new TokenController());
            applicationContext.put("/api", new TokenController());
            applicationContext.put("/chart.do", new ChartController());
            applicationContext.put("/recommend.do", new RecommendController());
            
            // 플레이리스트 컨트롤러 등록
            System.out.println("[초기화] 플레이리스트 컨트롤러 등록");
            PlaylistController playlistController = new PlaylistController(playlistDAO);
            applicationContext.put("/playlist.do", playlistController);
            
            applicationContext.put("/profile.do", new ProfileController(userDAO));
            // 비밀번호 찾기 컨트롤러 등록
            applicationContext.put("/findPassword.do", new FindPasswordController(userDAO));
            
            System.out.println("[초기화] 애플리케이션 객체 생성 완료");
            
        } catch (Exception e) {
            System.err.println("[심각한 오류] 애플리케이션 객체 생성 실패: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("애플리케이션 객체 생성 실패", e);
        }
    }
    
    /**
     * 애플리케이션에서 사용할 객체들을 생성하고 의존성을 주입하는 메서드 (정적 버전)
     */
    private static void createApplicationObjectsStatic() {
        System.out.println("[초기화] 애플리케이션 객체 생성 시작 (정적)");
        
        try {
            // 데이터소스 가져오기
            DataSource ds = (DataSource) applicationContext.get("dataSource");
            
            if (ds == null) {
                throw new Exception("데이터소스가 초기화되지 않았습니다 (정적).");
            }
            
            // DAO 객체 생성
            System.out.println("[초기화] DAO 객체 생성 (정적)");
            UserDAO userDAO = new UserDAO(ds);
            PlaylistDAO playlistDAO = new PlaylistDAO(ds);
            
            // 컨트롤러 객체 생성 및 DAO 의존성 주입
            System.out.println("[초기화] 컨트롤러 객체 생성 (정적)");
            applicationContext.put("/index.do", new IndexController());
            applicationContext.put("/auth/login.do", new AuthController(userDAO));
            applicationContext.put("/signup.do", new SignupController(userDAO));
            applicationContext.put("/search.do", new SearchController());
            applicationContext.put("/token.do", new TokenController());
            applicationContext.put("/api", new TokenController());
            applicationContext.put("/chart.do", new ChartController());
            applicationContext.put("/recommend.do", new RecommendController());
            
            // 플레이리스트 컨트롤러 등록
            System.out.println("[초기화] 플레이리스트 컨트롤러 등록 (정적)");
            PlaylistController playlistController = new PlaylistController(playlistDAO);
            applicationContext.put("/playlist.do", playlistController);
            
            applicationContext.put("/profile.do", new ProfileController(userDAO));
            // 비밀번호 찾기 컨트롤러 등록
            applicationContext.put("/findPassword.do", new FindPasswordController(userDAO));
            
            System.out.println("[초기화] 애플리케이션 객체 생성 완료 (정적)");
            
        } catch (Exception e) {
            System.err.println("[심각한 오류] 애플리케이션 객체 생성 실패 (정적): " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("애플리케이션 객체 생성 실패 (정적)", e);
        }
    }
    
    /**
     * SimpleDataSource 클래스 - JNDI 데이터소스가 없을 때 사용하는 기본 데이터소스
     */
    public static class SimpleDataSource implements DataSource {
        private final String jdbcUrl = "jdbc:mysql://localhost:3306/musicdb?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
        private final String username = "root";
        private final String password = "mysql";
        private final String driver = "com.mysql.cj.jdbc.Driver";
        
        /**
         * 기본 생성자로 드라이버 로드
         */
        public SimpleDataSource() {
            try {
                System.out.println("[SimpleDataSource] 초기화 - JDBC 드라이버 로드: " + driver);
                Class.forName(driver);
                System.out.println("[SimpleDataSource] 초기화 - JDBC URL: " + jdbcUrl);
            } catch (ClassNotFoundException e) {
                System.err.println("[심각한 오류] JDBC 드라이버 로드 실패: " + e.getMessage());
                e.printStackTrace();
                ErrorLogger.logError("JDBC 드라이버 로드 실패", e);
            }
        }
        
        /**
         * 데이터베이스 연결을 제공하는 메서드
         */
        @Override
        public Connection getConnection() throws SQLException {
            System.out.println("[SimpleDataSource] 데이터베이스 연결 요청");
            
            try {
                Properties props = new Properties();
                props.setProperty("user", username);
                props.setProperty("password", password);
                props.setProperty("useUnicode", "true");
                props.setProperty("characterEncoding", "UTF-8");
                props.setProperty("serverTimezone", "Asia/Seoul");
                
                Connection conn = java.sql.DriverManager.getConnection(jdbcUrl, props);
                System.out.println("[SimpleDataSource] 데이터베이스 연결 성공");
                return conn;
            } catch (SQLException e) {
                System.err.println("[SimpleDataSource] 데이터베이스 연결 실패: " + e.getMessage());
                ErrorLogger.logError("데이터베이스 연결 실패", e);
                throw e;
            }
        }
        
        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");
            props.setProperty("serverTimezone", "Asia/Seoul");
            
            return java.sql.DriverManager.getConnection(jdbcUrl, props);
        }
        
        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }
        
        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            // Not implemented
        }
        
        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            // Not implemented
        }
        
        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }
        
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (iface.isAssignableFrom(getClass())) {
                return iface.cast(this);
            }
            throw new SQLException("Cannot unwrap to " + iface.getName());
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return iface.isAssignableFrom(getClass());
        }
        
        // Java 7+ method (required for JDBC 4.1 compatibility)
        public Logger getParentLogger() {
            return Logger.getLogger("global");
        }
    }
} 