<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %><%@ page import="java.util.List, java.util.Map" %><%
// 출력 버퍼 초기화
out.clearBuffer();

// Content-Type 헤더 명시적 설정
response.setContentType("application/json");
response.setHeader("Cache-Control", "no-cache");

// 모델에서 트랙 정보 리스트 가져오기
List<Map<String, Object>> tracks = (List<Map<String, Object>>) request.getAttribute("tracks");
String playlistId = (String) request.getAttribute("playlistId");
String playlistName = (String) request.getAttribute("playlistName");

// 트랙 수 및 미리듣기 가능 트랙 수 계산
int totalTracks = tracks != null ? tracks.size() : 0;
int previewableTracks = 0;
if (tracks != null) {
    for (Map<String, Object> track : tracks) {
        if (track.get("preview_url") != null && 
            !String.valueOf(track.get("preview_url")).isEmpty() && 
            !String.valueOf(track.get("preview_url")).contains("open.spotify.com")) {
            previewableTracks++;
        }
    }
}

// 직접 JSON 문자열 생성
StringBuilder json = new StringBuilder();
json.append("{");
json.append("\"status\":\"OK\",");
json.append("\"playlistId\":\"").append(escapeJson(playlistId)).append("\",");
json.append("\"playlistName\":\"").append(escapeJson(playlistName)).append("\",");
json.append("\"total\":").append(totalTracks).append(",");
json.append("\"previewable\":").append(previewableTracks).append(",");
json.append("\"tracks\":[");

if (tracks != null && !tracks.isEmpty()) {
    for (int i = 0; i < tracks.size(); i++) {
        Map<String, Object> track = tracks.get(i);
        String previewUrl = track.get("preview_url") != null ? String.valueOf(track.get("preview_url")) : "";
        boolean hasPreview = previewUrl != null && !previewUrl.isEmpty() && !previewUrl.contains("open.spotify.com");
        
        json.append("{");
        json.append("\"id\":\"").append(escapeJson(track.get("id"))).append("\",");
        json.append("\"name\":\"").append(escapeJson(track.get("name"))).append("\",");
        json.append("\"artist\":\"").append(escapeJson(track.get("artist"))).append("\",");
        json.append("\"preview_url\":\"").append(escapeJson(track.get("preview_url"))).append("\",");
        json.append("\"has_preview\":").append(hasPreview).append("");
        
        // 이미지 URL 추가
        if (track.containsKey("image_url") && track.get("image_url") != null) {
            json.append(",\"image_url\":\"").append(escapeJson(track.get("image_url"))).append("\"");
        }
        
        // Spotify URL 추가
        if (track.containsKey("spotify_url") && track.get("spotify_url") != null) {
            json.append(",\"spotify_url\":\"").append(escapeJson(track.get("spotify_url"))).append("\"");
        } else if (track.containsKey("external_urls") && track.get("external_urls") != null) {
            json.append(",\"spotify_url\":\"").append(escapeJson(track.get("external_urls"))).append("\"");
        }
        
        json.append("}");
        if (i < tracks.size() - 1) {
            json.append(",");
        }
    }
}

json.append("]");
json.append("}");

// 단일 출력 - 이전에 출력된 내용이 없도록 함
%><%=json.toString()%><%!
// JSON 문자열 이스케이프 처리 메소드
private String escapeJson(Object value) {
    if (value == null) return "";
    return String.valueOf(value)
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
}
%> 