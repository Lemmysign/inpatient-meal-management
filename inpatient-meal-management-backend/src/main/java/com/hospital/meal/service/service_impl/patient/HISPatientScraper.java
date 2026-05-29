package com.hospital.meal.service.service_impl.patient;

import com.hospital.meal.config.HISProperties;
import com.hospital.meal.dto.patient.PatientInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Scrapes the HIS Patient Lists page to find a patient by UHID.
 * Stops scraping as soon as the matching UHID row is found.
 * Handles pagination automatically — loops until patient is found
 * or no more pages exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HISPatientScraper {

    private final HISProperties hisProperties;
    private final HISSessionManager sessionManager;

    // Expected column headers for validation
    private static final String[] EXPECTED_HEADERS = {
            "UHID", "IP No.", "Patient Name", "Gender/Age",
            "Admission Date", "Bed No"
    };

    // Patient list page URL
    private static final String PATIENT_LIST_PATH = "/EMRBILLING/PatientLists.aspx";

    /**
     * Finds a patient by UHID across all pages of the HIS patient list.
     * Returns PatientInfo if found, null if not found.
     */
    public PatientInfo findPatientByUHID(String uhid) throws IOException {
        log.info("Searching HIS patient list for UHID: {}", uhid);

        Map<String, String> cookies = sessionManager.getSessionCookies();
        String url = hisProperties.getBaseUrl() + PATIENT_LIST_PATH;

        // Load first page
        Document doc = fetchPage(url, cookies);

        // Check session expiry
        if (sessionManager.isSessionExpired(doc)) {
            log.warn("Session expired before scraping. Re-logging in...");
            sessionManager.reLogin();
            cookies = sessionManager.getSessionCookies();
            doc = fetchPage(url, cookies);
        }

        // Validate table headers on first load
        validateTableHeaders(doc);

        int pageNumber = 1;

        while (true) {
            log.debug("Scanning page {}...", pageNumber);

            // Search current page for UHID
            PatientInfo found = searchTableForUHID(doc, uhid);
            if (found != null) {
                log.info("Patient found on page {} for UHID: {}", pageNumber, uhid);
                return found;
            }

            // Check if next page exists using pager info
            if (!hasNextPage(doc, pageNumber)) {
                log.info("No more pages. Patient not found for UHID: {}", uhid);
                return null;
            }

            // Go to next page via postback
            pageNumber++;
            doc = goToNextPage(url, doc, cookies);

            // Check session expiry after postback
            if (sessionManager.isSessionExpired(doc)) {
                log.warn("Session expired during pagination. Re-logging in...");
                sessionManager.reLogin();
                cookies = sessionManager.getSessionCookies();
                doc = fetchPage(url, cookies);
                pageNumber = 1;
            }
        }
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────

    /**
     * GETs the patient list page with current session cookies.
     */
    private Document fetchPage(String url, Map<String, String> cookies) throws IOException {
        return Jsoup.connect(url)
                .cookies(cookies)
                .timeout(hisProperties.getReadTimeoutMs())
                .get();
    }

    /**
     * Searches the current page's table rows for a matching UHID.
     * Returns PatientInfo if found, null otherwise.
     */
    private PatientInfo searchTableForUHID(Document doc, String targetUhid) {
        Elements rowContainers = doc.select(
                "[id~=ctl00_ContentPlaceHolder1_gvAdmission_ctl00__\\d+]"
        );

        for (Element row : rowContainers) {
            Element uhidEl = row.selectFirst("[id$=_lblRegistrationNo]");
            Element nameEl = row.selectFirst("[id$=_lblName]");
            Element bedEl = row.selectFirst("[id$=_lnkCurrentBedNo]");

            if (uhidEl == null || nameEl == null || bedEl == null) continue;

            String rowUhid = uhidEl.text().trim();

            if (targetUhid.trim().equalsIgnoreCase(rowUhid)) {
                String fullName = nameEl.text().trim();
                String bedNo = bedEl.text().trim();

                fullName = stripTitle(fullName);
                String[] nameParts = fullName.split("\\s+", 2);
                String firstName = nameParts.length > 0 ? nameParts[0] : fullName;
                String lastName = nameParts.length > 1 ? nameParts[1] : "";

                return PatientInfo.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .roomNumber(bedNo)
                        .build();
            }
        }
        return null;
    }

    /**
     * Posts to the next page via ASP.NET __doPostBack mechanism.
     */
    private Document goToNextPage(String url, Document currentDoc,
                                  Map<String, String> cookies) throws IOException {

        String viewState = extractHiddenField(currentDoc, "__VIEWSTATE");
        String viewStateGen = extractHiddenField(currentDoc, "__VIEWSTATEGENERATOR");
        String eventValidation = extractHiddenField(currentDoc, "__EVENTVALIDATION");

        return Jsoup.connect(url)
                .cookies(cookies)
                .timeout(hisProperties.getReadTimeoutMs())
                .method(Connection.Method.POST)
                .data("__EVENTTARGET",
                        "ctl00$ContentPlaceHolder1$gvAdmission$ctl00$ctl03$ctl01$ctl07")
                .data("__EVENTARGUMENT", "")
                .data("__VIEWSTATE", viewState)
                .data("__VIEWSTATEGENERATOR", viewStateGen)
                .data("__EVENTVALIDATION", eventValidation)
                .post();
    }

    /**
     * Checks if there is a next page by reading the pager info text.
     * The pager contains: "X items in Y pages"
     * Compares current page number against total pages.
     */
    private boolean hasNextPage(Document doc, int currentPage) {
        try {
            Element pagerTable = doc.selectFirst(
                    "table[id*=gvAdmission][id*=Pager]");

            if (pagerTable == null) {
                log.warn("Pager table not found — assuming no more pages.");
                return false;
            }

            // Extract <strong> elements — [0]=total items, [1]=total pages
            Elements strongEls = pagerTable.select("strong");
            if (strongEls.size() < 2) {
                log.warn("Could not read total pages from pager — assuming no more pages.");
                return false;
            }

            int totalPages = Integer.parseInt(strongEls.get(1).text().trim());
            log.debug("Pager: current page={}, total pages={}", currentPage, totalPages);

            return currentPage < totalPages;

        } catch (Exception e) {
            log.warn("Error reading pager info: {} — assuming no more pages.", e.getMessage());
            return false;
        }
    }

    /**
     * Validates that the table column headers match what we expect.
     * Throws an exception if the table structure has changed.
     */
    private void validateTableHeaders(Document doc) {
        Elements headers = doc.select("table tr th");
        if (headers.isEmpty()) {
            log.warn("Could not find table headers to validate — proceeding anyway.");
            return;
        }

        for (int i = 0; i < Math.min(EXPECTED_HEADERS.length, headers.size()); i++) {
            String actual = headers.get(i).text().trim();
            String expected = EXPECTED_HEADERS[i];
            if (!actual.equalsIgnoreCase(expected)) {
                log.error("HIS table structure changed! Expected column {}: '{}' but got '{}'",
                        i, expected, actual);
                throw new IllegalStateException(
                        "HIS patient table structure has changed. " +
                                "Column " + i + " expected '" + expected +
                                "' but got '" + actual + "'. Please update the scraper.");
            }
        }
        log.debug("HIS table headers validated successfully.");
    }

    /**
     * Strips common title prefixes from a name.
     */
    private String stripTitle(String name) {
        return name.replaceFirst("(?i)^(Mrs?\\.|Dr\\.|Prof\\.|Alhaja|Baby of)\\s*", "").trim();
    }

    /**
     * Extracts a hidden input field value from the HTML document.
     */
    private String extractHiddenField(Document doc, String fieldName) {
        Element element = doc.selectFirst("input[name=" + fieldName + "]");
        return element != null ? element.val() : "";
    }
}