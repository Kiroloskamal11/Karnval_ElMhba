package com.church.karneval.service.impl;

import com.church.karneval.dto.AuthResponse;
import com.church.karneval.dto.RegisterRequest;
import com.church.karneval.enums.UserRole;
import com.church.karneval.enums.UserStatus;
import com.church.karneval.model.Station;
import com.church.karneval.model.Team;
import com.church.karneval.model.User;
import com.church.karneval.enums.NotificationType;
import com.church.karneval.repository.StationRepository;
import com.church.karneval.repository.TeamRepository;
import com.church.karneval.repository.UserRepository;
import com.church.karneval.service.AuthService;
import com.church.karneval.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final StationRepository stationRepository;
    private final NotificationService notificationService;

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.anon-key:}")
    private String supabaseAnonKey;

    public AuthServiceImpl(
            UserRepository userRepository,
            TeamRepository teamRepository,
            StationRepository stationRepository,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.stationRepository = stationRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("البريد الإلكتروني مسجل بالفعل.");
        }

        // Validate role-specific requirements (per SRS & database schema constraints)
        if (request.getRole() == UserRole.TEAM_LEADER && request.getTeamId() == null) {
            throw new RuntimeException("قائد الفريق يجب أن يختار لون الفريق.");
        }
        if (request.getRole() == UserRole.CAMP_LEADER && request.getStationId() == null) {
            throw new RuntimeException("مسئول المحطة يجب أن يختار المحطة الخاصة به.");
        }

        try {
            String signupUri = supabaseUrl + "/auth/v1/signup";
            logger.info("[REGISTER] Supabase signup URL: {}", signupUri);
            logger.debug("[REGISTER] supabaseUrl='{}', anonKey length={}",
                    supabaseUrl, supabaseAnonKey != null ? supabaseAnonKey.length() : 0);

            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseAnonKey);

            // Build request body as a Map (Spring will serialize to JSON properly)
            Map<String, String> body = new HashMap<>();
            body.put("email", request.getEmail());
            body.put("password", request.getPassword());

            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    signupUri, HttpMethod.POST, httpEntity, String.class);

            logger.info("[REGISTER] Supabase response status: {}", response.getStatusCode());
            logger.debug("[REGISTER] Supabase response body: {}", response.getBody());

            int statusCode = response.getStatusCode().value();
            if (statusCode != 200 && statusCode != 201) {
                String errMsg = extractJsonField(response.getBody(), "msg");
                if (errMsg == null)
                    errMsg = "فشل إنشاء الحساب في Supabase";
                throw new RuntimeException("فشل التسجيل: " + errMsg);
            }

            String responseBody = response.getBody();
            String uuidStr = extractUserId(responseBody);

            if (uuidStr == null) {
                throw new RuntimeException(
                        "لم يتم العثور على معرف المستخدم في استجابة Supabase.");
            }

            UUID userUuid = UUID.fromString(uuidStr);

            User user = new User();
            user.setId(userUuid);
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setRole(request.getRole());
            
            // Auto-approve the first SUPER_ADMIN or test super admins
            if (request.getRole() == UserRole.SUPER_ADMIN && 
               (userRepository.findByRole(UserRole.SUPER_ADMIN).isEmpty() || request.getEmail().contains("test"))) {
                user.setStatus(UserStatus.APPROVED);
            } else {
                user.setStatus(UserStatus.PENDING);
            }

            if (request.getTeamId() != null) {
                Optional<Team> teamOpt = teamRepository.findById(request.getTeamId());
                teamOpt.ifPresent(user::setTeam);
            }

            if (request.getStationId() != null) {
                Optional<Station> stationOpt = stationRepository.findById(request.getStationId());
                stationOpt.ifPresent(user::setStation);
            }

            user.setCreatedAt(OffsetDateTime.now());
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);

            notificationService.createNotificationForSuperAdminsOnly(
                    NotificationType.NEW_REGISTRATION,
                    "طلب تسجيل جديد",
                    "قام " + user.getName() + " بالتسجيل كـ " + user.getRole().name());

            return new AuthResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus(),
                    "تم تسجيل الحساب بنجاح وهو قيد الانتظار لمراجعة المسؤول.");

        } catch (HttpClientErrorException e) {
            logger.error("[REGISTER] Supabase HTTP error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            String errMsg = extractJsonField(e.getResponseBodyAsString(), "msg");
            if (errMsg == null)
                errMsg = "فشل إنشاء الحساب في Supabase: " + e.getStatusCode();
            throw new RuntimeException("فشل التسجيل: " + errMsg);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[REGISTER] Unexpected error: ", e);
            throw new RuntimeException(
                    "خطأ أثناء عملية التسجيل: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AuthResponse login(String email, String password) {
        try {

            String loginUri = supabaseUrl + "/auth/v1/token?grant_type=password";
            logger.info("[LOGIN] Supabase login URL: {}", loginUri);
            logger.debug("[LOGIN] supabaseUrl='{}', anonKey length={}",
                    supabaseUrl, supabaseAnonKey != null ? supabaseAnonKey.length() : 0);

            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseAnonKey);

            // Build request body as a Map
            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);

            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    loginUri, HttpMethod.POST, httpEntity, String.class);

            logger.info("[LOGIN] Supabase response status: {}", response.getStatusCode());
            logger.debug("[LOGIN] Supabase response body: {}", response.getBody());

            int statusCode = response.getStatusCode().value();
            if (statusCode != 200 && statusCode != 201) {
                String errMsg = extractJsonField(response.getBody(), "error_description");
                if (errMsg == null)
                    errMsg = extractJsonField(response.getBody(), "msg");
                if (errMsg == null)
                    errMsg = "فشل تسجيل الدخول. تحقق من البريد الإلكتروني وكلمة المرور.";
                throw new RuntimeException(errMsg);
            }

            String responseBody = response.getBody();
            String accessToken = extractJsonField(responseBody, "access_token");
            String uuidStr = extractUserId(responseBody);

            if (uuidStr == null) {
                throw new RuntimeException(
                        "لم يتم العثور على معرف المستخدم في استجابة تسجيل الدخول.");
            }

            UUID userUuid = UUID.fromString(uuidStr);
            User user = userRepository.findById(userUuid)
                    .orElseThrow(() -> new RuntimeException(
                            "المستخدم غير موجود بقاعدة البيانات المحلية."));

            if (user.getStatus() == UserStatus.PENDING) {
                throw new RuntimeException("حسابك قيد المراجعة. انتظر موافقة المسؤول.");
            }
            if (user.getStatus() == UserStatus.REJECTED) {
                throw new RuntimeException("تم رفض طلب تسجيلك.");
            }

            return new AuthResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus(),
                    accessToken,
                    user.getTeam(),
                    user.getStation());

        } catch (HttpClientErrorException e) {
            logger.error("[LOGIN] Supabase HTTP error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            String errMsg = extractJsonField(e.getResponseBodyAsString(), "error_description");
            if (errMsg == null)
                errMsg = extractJsonField(e.getResponseBodyAsString(), "msg");
            if (errMsg == null)
                errMsg = "فشل تسجيل الدخول: " + e.getStatusCode();
            throw new RuntimeException(errMsg);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[LOGIN] Unexpected error: ", e);
            throw new RuntimeException(
                    "خطأ أثناء عملية تسجيل الدخول: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the user ID from a Supabase response body.
     * First tries the nested "user" object, then falls back to top-level "id".
     */
    private String extractUserId(String responseBody) {
        if (responseBody == null)
            return null;

        String uuidStr = null;
        String userBlock = extractJsonObject(responseBody, "user");
        if (userBlock != null) {
            uuidStr = extractJsonField(userBlock, "id");
        }
        if (uuidStr == null) {
            uuidStr = extractJsonField(responseBody, "id");
        }
        return uuidStr;
    }

    private String extractJsonField(String json, String key) {
        if (json == null)
            return null;
        Pattern p = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*\"(.*?)(?<!\\\\)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private String extractJsonObject(String json, String key) {
        if (json == null)
            return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0)
            return null;
        int braceStart = json.indexOf('{', idx + search.length());
        if (braceStart < 0)
            return null;
        int depth = 0;
        for (int i = braceStart; i < json.length(); i++) {
            if (json.charAt(i) == '{')
                depth++;
            else if (json.charAt(i) == '}') {
                depth--;
                if (depth == 0)
                    return json.substring(braceStart, i + 1);
            }
        }
        return null;
    }
}