package com.hospital.meal.service.service_impl.patient;

import com.hospital.meal.config.HISProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the HIS session lifecycle:
 * - Initial login
 * - Cookie storage
 * - Session keep-alive (every 8 minutes)
 * - Expiry detection and auto re-login
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HISSessionManager {

    private final HISProperties hisProperties;

    /** Stored session cookies after successful login */
    private Map<String, String> sessionCookies = new HashMap<>();

    /** Lock to prevent concurrent re-login attempts */
    private final ReentrantLock loginLock = new ReentrantLock();

    /** Flag to track if we are currently logged in */
    private volatile boolean loggedIn = false;

    // ─────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────

    /**
     * Returns current session cookies.
     * If not logged in, performs login first.
     */
    public Map<String, String> getSessionCookies() throws IOException {
        if (!loggedIn || sessionCookies.isEmpty()) {
            login();
        }
        return sessionCookies;
    }

    /**
     * Checks if the given HTML response is actually a login page,
     * which means the session has expired.
     */
    public boolean isSessionExpired(Document document) {
        // If the login form is present, we've been redirected to login page
        return document.selectFirst("input#txtUserID") != null;
    }

    /**
     * Forces a fresh login and refreshes cookies.
     * Called automatically when session expiry is detected.
     */
    public void reLogin() throws IOException {
        log.warn("HIS session expired. Re-logging in...");
        loggedIn = false;
        sessionCookies.clear();
        login();
    }

    // ─────────────────────────────────────────────
    // KEEP-ALIVE SCHEDULER
    // ─────────────────────────────────────────────

    /**
     * Pings the HIS Patient Lists page every 8 minutes
     * to prevent session timeout (HIS timeout = 10 minutes).
     * Runs only if currently logged in.
     */
    @Scheduled(fixedDelayString = "#{${his.scraper.keep-alive-interval-minutes:8} * 60 * 1000}")
    public void keepAlive() {
        if (!loggedIn || sessionCookies.isEmpty()) {
            log.debug("HIS keep-alive skipped — not currently logged in.");
            return;
        }

        try {
            log.debug("HIS keep-alive ping...");
            String patientListUrl = hisProperties.getBaseUrl()
                    + "/EMRBILLING/PatientLists.aspx";

            Document doc = Jsoup.connect(patientListUrl)
                    .cookies(sessionCookies)
                    .timeout(hisProperties.getConnectionTimeoutMs())
                    .get();

            if (isSessionExpired(doc)) {
                log.warn("HIS keep-alive detected session expiry. Re-logging in...");
                reLogin();
            } else {
                log.debug("HIS keep-alive successful — session is alive.");
            }

        } catch (IOException e) {
            log.error("HIS keep-alive failed: {}. Will attempt re-login on next request.", e.getMessage());
            loggedIn = false;
        }
    }

    // ─────────────────────────────────────────────
    // PRIVATE — LOGIN LOGIC
    // ─────────────────────────────────────────────
    private void login() throws IOException {
        loginLock.lock();
        try {
            if (loggedIn && !sessionCookies.isEmpty()) {
                return;
            }

            log.info("Logging into HIS...");

            String loginUrl = hisProperties.getBaseUrl() + "/login.aspx?Logout=1";

            // Step 1 — GET login page to extract ViewState
            Connection.Response loginPage = Jsoup.connect(loginUrl)
                    .timeout(hisProperties.getConnectionTimeoutMs())
                    .method(Connection.Method.GET)
                    .execute();

            Document loginDoc = loginPage.parse();

            String viewState = extractHiddenField(loginDoc, "__VIEWSTATE");
            String viewStateGenerator = extractHiddenField(loginDoc, "__VIEWSTATEGENERATOR");
            String scrollX = extractHiddenField(loginDoc, "__SCROLLPOSITIONX");
            String scrollY = extractHiddenField(loginDoc, "__SCROLLPOSITIONY");

            // Step 2 — POST credentials with followRedirects(false)
            // The redirect URL contains %Hospital% which is invalid URI encoding
            // and causes Jsoup to throw MalformedURLException when following it
            Connection.Response loginResponse = Jsoup.connect(loginUrl)
                    .timeout(hisProperties.getConnectionTimeoutMs())
                    .method(Connection.Method.POST)
                    .cookies(loginPage.cookies())
                    .followRedirects(false)
                    .data("__LASTFOCUS", "")
                    .data("__EVENTTARGET", "txtPassword")
                    .data("__EVENTARGUMENT", "")
                    .data("__VIEWSTATE", viewState)
                    .data("__VIEWSTATEGENERATOR", viewStateGenerator)
                    .data("__SCROLLPOSITIONX", scrollX != null ? scrollX : "0")
                    .data("__SCROLLPOSITIONY", scrollY != null ? scrollY : "0")
                    .data("RadFormDecorator1_ClientState", "")
                    .data("txtUserID", hisProperties.getUsername())
                    .data("txtPassword", hisProperties.getPassword())
                    .data("ddlFacility", "Facility")
                    .data("ddlFacility_ClientState", "")
                    .data("dropGroup", "Group")
                    .data("dropGroup_ClientState", "")
                    .data("ddlEntrySite", "EntrySite")
                    .data("ddlEntrySite_ClientState", "")
                    .data("hdnHspId", "")
                    .data("hdnPassowrdValidation", "")
                    .data("hdnAccountLockThreshold", "")
                    .data("RadWindow1_ClientState", "")
                    .data("RadWindowManager_ClientState", "")
                    .data("txtUserName", "")
                    .data("hdnMobileNo", "")
                    .data("btnLogin", "Login")
                    .execute();

            // Step 3 — A 302 redirect means login succeeded
            // Merge cookies from both GET and POST responses
            if (loginResponse.statusCode() == 302) {
                sessionCookies = new HashMap<>(loginPage.cookies());
                sessionCookies.putAll(loginResponse.cookies());
                loggedIn = true;
                log.info("HIS login successful. Session cookies stored.");
            } else {
                // Unexpected status — check if response is login page
                Document responseDoc = loginResponse.parse();
                if (isSessionExpired(responseDoc)) {
                    throw new IOException("HIS login failed — invalid credentials or server error.");
                }
                // 200 with non-login page also means success
                sessionCookies = new HashMap<>(loginPage.cookies());
                sessionCookies.putAll(loginResponse.cookies());
                loggedIn = true;
                log.info("HIS login successful (status 200). Session cookies stored.");
            }

        } finally {
            loginLock.unlock();
        }
    }
    /**
     * Extracts a hidden input field value from the HTML document.
     */
    private String extractHiddenField(Document doc, String fieldName) {
        var element = doc.selectFirst("input[name=" + fieldName + "]");
        return element != null ? element.val() : "";
    }
}