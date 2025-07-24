package dto;

/**
 * 사용자 정보를 담는 DTO 클래스
 * - users 테이블과 매핑
 */
public class UserDTO {
    private String password;  // 비밀번호
    private String email;     // 이메일 (기본 키)
    private String name;      // 이름
    private String role;      // 역할 (ADMIN, USER 등)
    private String phone;
    private String createdAt;
    
    // 기본 생성자
    public UserDTO() {}
    
    // DB 컬럼 기반 생성자
    public UserDTO(String email, String password, String name, String phone, String createdAt) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.createdAt = createdAt;
    }
    
    // Getter & Setter
    public String getPassword() {
        return password;
    }
    
    public UserDTO setPassword(String password) {
        this.password = password;
        return this;
    }
    
    public String getEmail() {
        return email;
    }
    
    public UserDTO setEmail(String email) {
        this.email = email;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public UserDTO setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getPhone() { return phone; }
    public UserDTO setPhone(String phone) { this.phone = phone; return this; }
    
    public String getCreatedAt() { return createdAt; }
    public UserDTO setCreatedAt(String createdAt) { this.createdAt = createdAt; return this; }
    
    /**
     * 사용자 아이디를 반환하는 메서드 (AuthController에서 사용하는 이름)
     * 
     * @return 사용자 이메일(아이디)
     */
    public String getUsername() {
        return email;
    }
} 