package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.ErrorLogger;
import utils.TokenManager;

/**
 * Spotify API 연동을 위한 서비스 클래스
 */
public class SpotifyService {
    
    private static final String API_BASE_URL = "https://api.spotify.com/v1";
    
    /**
     * 플레이리스트의 트랙 정보를 가져옵니다.
     * 
     * @param accessToken Spotify API 액세스 토큰
     * @param playlistId 플레이리스트 ID
     * @return 트랙 정보 리스트
     */
    public List<Map<String, Object>> getPlaylistTracks(String accessToken, String playlistId) {
        List<Map<String, Object>> tracks = new ArrayList<>();
        
        System.out.println("[SpotifyService] 플레이리스트 트랙 가져오기 시작: " + playlistId);
        
        try {
            // 최대 100곡까지 가져오도록 수정 (미리듣기 URL이 있는 트랙을 더 많이 찾기 위해)
            URL url = new URL(API_BASE_URL + "/playlists/" + playlistId + "/tracks?limit=100");
            System.out.println("[SpotifyService] API 요청 URL: " + url.toString());
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            
            int responseCode = conn.getResponseCode();
            System.out.println("[SpotifyService] API 응답 코드: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // JSON 응답 파싱
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray items = jsonResponse.getJSONArray("items");
                
                System.out.println("[SpotifyService] 가져온 총 트랙 수: " + items.length());
                int skippedTracks = 0;
                int addedTracks = 0;
                int addedWithoutPreview = 0;
                
                // 트랙 데이터 추출
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    
                    // 트랙 정보가 없는 경우 스킵
                    if (item.isNull("track")) {
                        System.out.println("[SpotifyService] 트랙 정보 없음 (인덱스: " + i + ")");
                        skippedTracks++;
                        continue;
                    }
                    
                    JSONObject track = item.getJSONObject("track");
                    
                    // 트랙 ID가 없는 경우 스킵
                    if (track.isNull("id")) {
                        System.out.println("[SpotifyService] 트랙 ID 없음");
                        skippedTracks++;
                        continue;
                    }
                    
                    String trackId = track.getString("id");
                    String trackName = track.getString("name");
                    
                    // 미리듣기 URL 확인
                    String previewUrl = null;
                    boolean hasPreviewUrl = false;
                    
                    if (!track.isNull("preview_url") && !track.getString("preview_url").equals("null")) {
                        previewUrl = track.getString("preview_url");
                        hasPreviewUrl = true;
                        System.out.println("[SpotifyService] 미리듣기 URL 있음: " + trackName);
                    } else {
                        System.out.println("[SpotifyService] 미리듣기 URL 없음: " + trackName);
                        // 미리듣기 URL 대신 Spotify 외부 URL 제공
                        if (!track.isNull("external_urls") && !track.getJSONObject("external_urls").isNull("spotify")) {
                            previewUrl = track.getJSONObject("external_urls").getString("spotify");
                            System.out.println("[SpotifyService] Spotify 외부 링크로 대체: " + previewUrl);
                            addedWithoutPreview++;
                        } else {
                            skippedTracks++;
                            continue;
                        }
                    }
                    
                    // 트랙 정보 추가
                    Map<String, Object> trackInfo = new HashMap<>();
                    trackInfo.put("id", trackId);
                    trackInfo.put("name", trackName);
                    trackInfo.put("preview_url", previewUrl);
                    trackInfo.put("has_preview", hasPreviewUrl); // 실제 미리듣기 URL인지 여부
                    
                    // 아티스트 정보
                    JSONArray artists = track.getJSONArray("artists");
                    if (artists.length() > 0) {
                        String artist = artists.getJSONObject(0).getString("name");
                        trackInfo.put("artist", artist);
                        System.out.println("[SpotifyService] 아티스트: " + artist);
                    } else {
                        trackInfo.put("artist", "Unknown Artist");
                        System.out.println("[SpotifyService] 아티스트 정보 없음");
                    }
                    
                    // 앨범 커버 이미지
                    if (!track.isNull("album") && !track.getJSONObject("album").isNull("images")) {
                        JSONArray images = track.getJSONObject("album").getJSONArray("images");
                        if (images.length() > 0) {
                            String imageUrl = images.getJSONObject(0).getString("url");
                            trackInfo.put("image_url", imageUrl);
                            System.out.println("[SpotifyService] 앨범 이미지 URL: " + imageUrl);
                        } else {
                            System.out.println("[SpotifyService] 앨범 이미지 없음");
                        }
                    } else {
                        System.out.println("[SpotifyService] 앨범 정보 없음");
                    }
                    
                    // Spotify 외부 URL 추가
                    if (!track.isNull("external_urls") && !track.getJSONObject("external_urls").isNull("spotify")) {
                        String spotifyUrl = track.getJSONObject("external_urls").getString("spotify");
                        trackInfo.put("spotify_url", spotifyUrl);
                    }
                    
                    tracks.add(trackInfo);
                    addedTracks++;
                }
                
                System.out.println("[SpotifyService] 처리 결과 - 총 트랙: " + items.length() + 
                                  ", 스킵된 트랙: " + skippedTracks + 
                                  ", 추가된 트랙: " + addedTracks + 
                                  ", 미리듣기 없이 추가된 트랙: " + addedWithoutPreview);
                
            } else {
                System.err.println("[SpotifyService] Spotify API 호출 실패: " + responseCode);
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String errorLine;
                StringBuilder errorResponse = new StringBuilder();
                
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                
                System.err.println("[SpotifyService] 에러 응답: " + errorResponse.toString());
                ErrorLogger.logError("Spotify API 호출 오류 (플레이리스트 ID: " + playlistId + ")", 
                        new Exception("API 응답 코드: " + responseCode));
            }
            
        } catch (Exception e) {
            System.err.println("[SpotifyService] Spotify API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("Spotify API 호출 중 예외 발생", e);
        }
        
        return tracks;
    }
    
    /**
     * 트랙의 미리듣기 URL을 가져옵니다.
     * 
     * @param accessToken Spotify API 액세스 토큰
     * @param trackId 트랙 ID
     * @return 미리듣기 URL (없는 경우 null)
     */
    public String getTrackPreviewUrl(String accessToken, String trackId) {
        try {
            URL url = new URL(API_BASE_URL + "/tracks/" + trackId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // JSON 응답 파싱
                JSONObject track = new JSONObject(response.toString());
                
                // 미리듣기 URL 반환
                if (!track.isNull("preview_url")) {
                    return track.getString("preview_url");
                }
            }
            
        } catch (Exception e) {
            System.err.println("트랙 정보 조회 중 오류 발생: " + e.getMessage());
            ErrorLogger.logError("트랙 미리듣기 URL 조회 중 오류 (트랙 ID: " + trackId + ")", e);
        }
        
        return null;
    }

    /**
     * 플레이리스트 이름을 가져옵니다.
     * 
     * @param playlistId 플레이리스트 ID
     * @return 플레이리스트 이름 (가져오기 실패 시 null)
     */
    public String getPlaylistName(String playlistId) {
        String accessToken = null;
        
        try {
            // 토큰 매니저를 통해 토큰 가져오기 시도
            accessToken = TokenManager.getAccessToken();
        } catch (Exception e) {
            System.err.println("토큰 가져오기 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 토큰을 가져오지 못한 경우
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("Spotify 액세스 토큰을 가져올 수 없습니다.");
            return null;
        }
        
        try {
            URL url = new URL(API_BASE_URL + "/playlists/" + playlistId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // JSON 응답 파싱
                JSONObject jsonResponse = new JSONObject(response.toString());
                
                if (!jsonResponse.isNull("name")) {
                    return jsonResponse.getString("name");
                }
            } else {
                System.err.println("플레이리스트 정보 가져오기 실패: " + responseCode);
                // 오류 로그 기록
                ErrorLogger.logError("Spotify API 호출 오류 (플레이리스트 ID: " + playlistId + ")", 
                        new Exception("API 응답 코드: " + responseCode));
            }
        } catch (Exception e) {
            System.err.println("플레이리스트 정보 가져오기 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // 오류 로그 기록
            ErrorLogger.logError("Spotify API 호출 중 예외 발생", e);
        }
        
        return null;
    }
} 