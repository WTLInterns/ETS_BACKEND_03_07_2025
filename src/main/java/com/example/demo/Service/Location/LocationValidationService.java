package com.example.demo.Service.Location;


import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocationValidationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationValidationService.class);

    @Value("${google.maps.api.key:AIzaSyCelDo4I5cPQ72TfCTQW-arhPZ7ALNcp8w}")
    private String apiKey;

    private final RestTemplate restTemplate;

    private final Map<String, double[]> coordinateCache = new ConcurrentHashMap<>();


    // Pune Geographic Boundaries (Production-accurate)
    private static final class PuneBounds {
        // Core Pune City
        static final double CORE_LAT_MIN = 18.3500;
        static final double CORE_LAT_MAX = 18.7000;
        static final double CORE_LNG_MIN = 73.6500;
        static final double CORE_LNG_MAX = 74.1500;

        // Pune Metropolitan Region (PMR)
        static final double PMR_LAT_MIN = 18.2000;
        static final double PMR_LAT_MAX = 18.8500;
        static final double PMR_LNG_MIN = 73.5000;
        static final double PMR_LNG_MAX = 74.3500;

        // Extended areas (Kharadi, Wagholi, etc.)
        static final double EXTENDED_LAT_MIN = 18.1500;
        static final double EXTENDED_LAT_MAX = 18.9000;
        static final double EXTENDED_LNG_MIN = 73.4500;
        static final double EXTENDED_LNG_MAX = 74.4000;

        // Pimpri-Chinchwad
        static final double PCMC_LAT_MIN = 18.5500;
        static final double PCMC_LAT_MAX = 18.7500;
        static final double PCMC_LNG_MIN = 73.7000;
        static final double PCMC_LNG_MAX = 73.9000;
    }

    // Known Pune areas database
    private Set<String> knownPuneAreas;
    private Set<String> knownPunePostalCodes;
    private Set<String> puneKeywords;

    public LocationValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initializeKnownLocations() {
        try {
            // Use List first, then convert to Set to handle any potential duplicates gracefully
            List<String> puneAreasList = Arrays.asList(
                    // Central Pune
                    "deccan", "shivajinagar", "camp", "koregaon park", "pune station",
                    "swargate", "market yard", "kasba peth", "sadashiv peth", "narayan peth",

                    // West Pune
                    "kothrud", "karve nagar", "warje", "bavdhan", "pashan", "baner",
                    "aundh", "sus", "balewadi", "hinjewadi", "wakad", "pimple saudagar",
                    "pimple nilakh", "pimple gurav", "ravet", "tathawade", "mulshi",

                    // East Pune
                    "kharadi", "viman nagar", "kalyani nagar", "mundhwa",
                    "hadapsar", "magarpatta", "amanora", "wagholi", "keshav nagar",
                    "lohegaon", "yerawada", "ghorpadi", "fatima nagar", "wanowrie",
                    "undri", "pisoli", "mohammadwadi", "fursungi", "manjri",

                    // South Pune
                    "katraj", "kondhwa", "bibvewadi", "dhankawadi", "sahakarnagar",
                    "balaji nagar", "ambegaon", "hingne", "uttam nagar", "sinhagad road",

                    // North Pune
                    "vishrantwadi", "khadki", "pune cantonment", "range hills",
                    "dapodi", "kasarwadi", "dehu road", "alandi",

                    // Pimpri-Chinchwad
                    "pimpri", "chinchwad", "nigdi", "akurdi", "pradhikaran", "bhosari",
                    "chakan", "talegaon", "pirangut", "kudalwadi",

                    // Additional areas that might be duplicates (handled gracefully)
                    "koregaon park", "pune camp", "shivaji nagar"
            );

            // Convert to Set - automatically removes duplicates
            knownPuneAreas = new HashSet<>(puneAreasList);

            // Pune postal codes (411xxx)
            List<String> postalCodesList = Arrays.asList(
                    "411001", "411002", "411003", "411004", "411005", "411006", "411007", "411008",
                    "411009", "411010", "411011", "411012", "411013", "411014", "411015", "411016",
                    "411017", "411018", "411019", "411020", "411021", "411022", "411023", "411024",
                    "411025", "411026", "411027", "411028", "411029", "411030", "411031", "411032",
                    "411033", "411034", "411035", "411036", "411037", "411038", "411039", "411040",
                    "411041", "411042", "411043", "411044", "411045", "411046", "411047", "411048",
                    "411051", "411052", "411057", "411061", "411062"
            );

            knownPunePostalCodes = new HashSet<>(postalCodesList);

            // Keywords that indicate Pune
            puneKeywords = Set.of("pune", "poona", "maharashtra", "pimpri", "chinchwad", "pcmc");

            logger.info("✅ Successfully initialized location validation with {} known areas, {} postal codes",
                    knownPuneAreas.size(), knownPunePostalCodes.size());

            // Log any duplicates that were removed
            int originalSize = puneAreasList.size();
            int finalSize = knownPuneAreas.size();
            if (originalSize != finalSize) {
                logger.warn("⚠️  Removed {} duplicate area names during initialization", originalSize - finalSize);
            }

        } catch (Exception e) {
            logger.error("❌ CRITICAL: Failed to initialize known locations: {}", e.getMessage(), e);

            // Fallback initialization with minimal set
            knownPuneAreas = Set.of("pune", "kharadi", "deccan", "hadapsar", "hinjewadi");
            knownPunePostalCodes = Set.of("411001", "411014", "411057");
            puneKeywords = Set.of("pune", "maharashtra");

            logger.warn("⚠️  Using fallback location data due to initialization error");
        }
    }

    /**
     * Main validation method with multiple fallback strategies
     */
    public LocationValidationResult validateLocation(String location, String locationType) {
        if (location == null || location.trim().isEmpty()) {
            return LocationValidationResult.error(
                    "MISSING_LOCATION",
                    locationType + " location is required"
            );
        }

        String cleanLocation = location.trim();
        logger.info("Validating {} location: '{}'", locationType, cleanLocation);

        try {
            // Strategy 1: Check known areas (fastest)
            if (isKnownPuneLocation(cleanLocation)) {
                logger.info("✅ Location '{}' validated via known areas database", cleanLocation);
                return LocationValidationResult.success("Location validated via known areas database");
            }

            // Strategy 2: Check postal codes
            if (hasValidPunePostalCode(cleanLocation)) {
                logger.info("✅ Location '{}' validated via postal code", cleanLocation);
                return LocationValidationResult.success("Location validated via postal code");
            }

            // Strategy 3: Check keywords
            if (containsPuneKeywords(cleanLocation)) {
                logger.info("✅ Location '{}' validated via keywords", cleanLocation);
                return LocationValidationResult.success("Location validated via keywords");
            }

            // Strategy 4: Geocoding with coordinates validation
            LocationValidationResult geocodingResult = validateViaGeocoding(cleanLocation, locationType);
            if (geocodingResult.isValid()) {
                return geocodingResult;
            }

            // Strategy 5: Fuzzy matching for slight variations
            if (hasFuzzyPuneMatch(cleanLocation)) {
                logger.info("✅ Location '{}' validated via fuzzy matching", cleanLocation);
                return LocationValidationResult.success("Location validated via fuzzy matching");
            }

            // All strategies failed
            logger.warn("❌ Location '{}' failed all validation strategies", cleanLocation);
            return LocationValidationResult.error(
                    locationType.toUpperCase() + "_LOCATION_OUT_OF_PUNE",
                    locationType + " location is outside Pune service area. Please select a " +
                            locationType + " location within Pune Metropolitan Region.",
                    Arrays.asList(
                            "Include 'Pune' or area name (e.g., 'Kharadi, Pune')",
                            "Use complete address with landmark",
                            "Ensure location is within Pune city limits",
                            "Try: 'Kothrud, Pune' or 'Hadapsar, Pune'"
                    )
            );

        } catch (Exception e) {
            logger.error("Error validating location '{}': {}", cleanLocation, e.getMessage(), e);
            return LocationValidationResult.error(
                    "VALIDATION_ERROR",
                    "Unable to validate location. Please try again with a complete address."
            );
        }
    }

    /**
     * Check if location is in known Pune areas database
     */
    private boolean isKnownPuneLocation(String location) {
        String locationLower = location.toLowerCase();

        // Direct match
        for (String area : knownPuneAreas) {
            if (locationLower.contains(area)) {
                logger.debug("Found known area '{}' in location '{}'", area, location);
                return true;
            }
        }

        return false;
    }

    /**
     * Check for valid Pune postal codes
     */
    private boolean hasValidPunePostalCode(String location) {
        for (String postalCode : knownPunePostalCodes) {
            if (location.contains(postalCode)) {
                logger.debug("Found postal code '{}' in location '{}'", postalCode, location);
                return true;
            }
        }
        return false;
    }

    /**
     * Check for Pune keywords
     */
    private boolean containsPuneKeywords(String location) {
        String locationLower = location.toLowerCase();

        for (String keyword : puneKeywords) {
            if (locationLower.contains(keyword)) {
                logger.debug("Found keyword '{}' in location '{}'", keyword, location);
                return true;
            }
        }
        return false;
    }

    /**
     * Fuzzy matching for slight variations
     */
    private boolean hasFuzzyPuneMatch(String location) {
        String locationLower = location.toLowerCase().replaceAll("[^a-z0-9]", "");

        // Check for common misspellings or variations
        Map<String, String> variations = Map.of(
                "kharadi", "karadi|kharadhi|kharadi",
                "hadapsar", "hadapsar|hadpsar|hadapasar",
                "hinjewadi", "hinjawadi|hinjewadi|hingewadi",
                "magarpatta", "magarpata|magarpatta|magar patta",
                "koregaon", "koregaon|koreagon|koregoan"
        );

        for (Map.Entry<String, String> entry : variations.entrySet()) {
            String[] patterns = entry.getValue().split("\\|");
            for (String pattern : patterns) {
                if (locationLower.contains(pattern)) {
                    logger.debug("Fuzzy match found: '{}' matches pattern '{}'", location, pattern);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Geocoding validation with robust error handling
     */
    private LocationValidationResult validateViaGeocoding(String location, String locationType) {
        try {
            double[] coordinates = getCoordinatesWithCache(location);

            if (coordinates == null) {
                logger.warn("Geocoding failed for location: '{}'", location);
                return LocationValidationResult.error(
                        "GEOCODING_FAILED",
                        "Unable to locate address. Please provide a more specific address."
                );
            }

            if (isWithinPuneBounds(coordinates[0], coordinates[1])) {
                logger.info("✅ Location '{}' validated via geocoding coordinates: {}, {}",
                        location, coordinates[0], coordinates[1]);

                // Cache this successful validation
                coordinateCache.put(location.toLowerCase().trim(), coordinates);

                return LocationValidationResult.success(
                        "Location validated via geocoding coordinates"
                );
            } else {
                logger.warn("❌ Location '{}' outside Pune bounds: lat={}, lng={}",
                        location, coordinates[0], coordinates[1]);
                return LocationValidationResult.error(
                        locationType.toUpperCase() + "_LOCATION_OUT_OF_PUNE",
                        locationType + " location coordinates are outside Pune service area."
                );
            }

        } catch (Exception e) {
            logger.error("Geocoding validation failed for '{}': {}", location, e.getMessage());
            return LocationValidationResult.error(
                    "GEOCODING_ERROR",
                    "Error during location validation. Please try again."
            );
        }
    }

    /**
     * Get coordinates with caching and error handling
     */
    @Cacheable(value = "coordinates", key = "#location.toLowerCase().trim()")
    public double[] getCoordinatesWithCache(String location) {
        String cacheKey = location.toLowerCase().trim();

        // Check local cache first
        if (coordinateCache.containsKey(cacheKey)) {
            logger.debug("Retrieved coordinates from cache for: '{}'", location);
            return coordinateCache.get(cacheKey);
        }

        try {
            String encodedLocation = UriUtils.encode(location, "UTF-8");
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
                    .queryParam("address", encodedLocation)
                    .queryParam("key", apiKey)
                    .queryParam("region", "in") // Bias results to India
                    .queryParam("components", "administrative_area:Maharashtra|country:IN") // Restrict to Maharashtra, India
                    .build()
                    .toString();

            logger.debug("Geocoding request URL: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                logger.error("Null response from Google Maps API for location: '{}'", location);
                return null;
            }

            String status = (String) response.get("status");
            logger.debug("Google Maps API status: {} for location: '{}'", status, location);

            if (!"OK".equals(status)) {
                handleGeocodingError(status, location);
                return null;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

            if (results == null || results.isEmpty()) {
                logger.warn("No results found for location: '{}'", location);
                return null;
            }

            // Get the first result
            Map<String, Object> firstResult = results.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> geometry = (Map<String, Object>) firstResult.get("geometry");
            @SuppressWarnings("unchecked")
            Map<String, Object> locationMap = (Map<String, Object>) geometry.get("location");

            double lat = ((Number) locationMap.get("lat")).doubleValue();
            double lng = ((Number) locationMap.get("lng")).doubleValue();

            double[] coordinates = new double[]{lat, lng};

            // Cache the result
            coordinateCache.put(cacheKey, coordinates);

            logger.info("Successfully geocoded '{}' to coordinates: lat={}, lng={}", location, lat, lng);
            return coordinates;

        } catch (Exception e) {
            logger.error("Exception during geocoding for location '{}': {}", location, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Handle different geocoding error statuses
     */
    private void handleGeocodingError(String status, String location) {
        switch (status) {
            case "ZERO_RESULTS":
                logger.warn("No geocoding results found for location: '{}'", location);
                break;
            case "OVER_QUERY_LIMIT":
                logger.error("Google Maps API quota exceeded");
                break;
            case "REQUEST_DENIED":
                logger.error("Google Maps API request denied - check API key and permissions");
                break;
            case "INVALID_REQUEST":
                logger.error("Invalid geocoding request for location: '{}'", location);
                break;
            default:
                logger.error("Unknown geocoding error status '{}' for location: '{}'", status, location);
        }
    }

    /**
     * Check if coordinates are within Pune bounds
     */
    private boolean isWithinPuneBounds(double lat, double lng) {
        // Check multiple boundary sets
        boolean inCore = isWithinBounds(lat, lng,
                PuneBounds.CORE_LAT_MIN, PuneBounds.CORE_LAT_MAX,
                PuneBounds.CORE_LNG_MIN, PuneBounds.CORE_LNG_MAX);

        boolean inPMR = isWithinBounds(lat, lng,
                PuneBounds.PMR_LAT_MIN, PuneBounds.PMR_LAT_MAX,
                PuneBounds.PMR_LNG_MIN, PuneBounds.PMR_LNG_MAX);

        boolean inExtended = isWithinBounds(lat, lng,
                PuneBounds.EXTENDED_LAT_MIN, PuneBounds.EXTENDED_LAT_MAX,
                PuneBounds.EXTENDED_LNG_MIN, PuneBounds.EXTENDED_LNG_MAX);

        boolean inPCMC = isWithinBounds(lat, lng,
                PuneBounds.PCMC_LAT_MIN, PuneBounds.PCMC_LAT_MAX,
                PuneBounds.PCMC_LNG_MIN, PuneBounds.PCMC_LNG_MAX);

        boolean result = inCore || inPMR || inExtended || inPCMC;

        logger.debug("Coordinate bounds check for lat={}, lng={}: Core={}, PMR={}, Extended={}, PCMC={}, Result={}",
                lat, lng, inCore, inPMR, inExtended, inPCMC, result);

        return result;
    }

    private boolean isWithinBounds(double lat, double lng, double latMin, double latMax, double lngMin, double lngMax) {
        return lat >= latMin && lat <= latMax && lng >= lngMin && lng <= lngMax;
    }

    /**
     * Public method for validating both pickup and drop locations
     */
    public LocationValidationResult validatePickupAndDropLocations(String pickUpLocation, String dropLocation) {
        // Validate pickup location
        LocationValidationResult pickupResult = validateLocation(pickUpLocation, "pickup");
        if (!pickupResult.isValid()) {
            return pickupResult;
        }

        // Validate drop location
        LocationValidationResult dropResult = validateLocation(dropLocation, "drop");
        if (!dropResult.isValid()) {
            return dropResult;
        }

        // Both valid
        logger.info("✅ Both locations validated successfully - Pickup: '{}', Drop: '{}'", pickUpLocation, dropLocation);
        return LocationValidationResult.success("Both pickup and drop locations validated successfully");
    }

    /**
     * Health check for the service
     */
    public boolean isServiceHealthy() {
        try {
            // Test with a known location
            double[] coords = getCoordinatesWithCache("Deccan, Pune");
            return coords != null && coords.length == 2;
        } catch (Exception e) {
            logger.error("Service health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clear coordinate cache (for maintenance)
     */
    public void clearCache() {
        coordinateCache.clear();
        logger.info("Coordinate cache cleared");
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", coordinateCache.size());
        stats.put("knownAreasCount", knownPuneAreas.size());
        stats.put("postalCodesCount", knownPunePostalCodes.size());
        return stats;
    }
}