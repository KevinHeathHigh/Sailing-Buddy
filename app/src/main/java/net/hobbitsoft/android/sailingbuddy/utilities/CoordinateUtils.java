package net.hobbitsoft.android.sailingbuddy.utilities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import net.hobbitsoft.android.sailingbuddy.data.DecimalCoordinates;
import net.hobbitsoft.android.sailingbuddy.database.StringCoordinates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CoordinateUtils {

    private static final String TAG = CoordinateUtils.class.getSimpleName();

    private static final String RAW_COORDINATE_REGEX_PATTERN =
            "\\(\\d*\\&#\\d*;\\d*'\\d*\"\\s*[N:S]*\\s*\\d*&#\\d*;\\d*'\\d*\"\\s[E:W]*\\)";
    private static final String RAW_LATITUDE_STRING_REGEX_PATTERN = "\\d*\\&#\\d*;\\d*'\\d*\"\\s*.[N:S]";
    private static final String RAW_LONGITUDE_STRING_REGEX_PATTERN = "\\d*\\&#\\d*;\\d*'\\d*\"\\s*.[E:W]";
    private static final String RAW_LATITUDE_DECIMAL_REGEX_PATTERN = "\\d*\\.\\d*\\s*.[N:S]";
    private static final String RAW_LONGITUDE_DECIMAL_REGEX_PATTERN = "\\d*\\.\\d*\\s*.[E:W]";
    private static final String DEGREE_SYMBOL_PATTERN = "&#176;";
    public static final String DEGREE_SYMBOL = "°";


    /**
     * @param coordinates
     * @return List of Strings (Longitude/Latitude 30°0'0", N 90°0'0" W
     */
    public static StringCoordinates resolveStringCoordinates(String coordinates) {
        //Get the actual coordinates from the string return from NOAA
        //30.000 N 90.000 W (30&#176;0'0" N 90&#176;0'0" W)
        StringCoordinates extractedCoordinates = new StringCoordinates();

        Pattern latitudeStringPattern = Pattern.compile(RAW_LATITUDE_STRING_REGEX_PATTERN);
        Matcher latitudeStringMatcher = latitudeStringPattern.matcher(coordinates);
        if (latitudeStringMatcher.find()) {
            extractedCoordinates.setLatitude(
                    getPrettyCoordinates(coordinates.substring(latitudeStringMatcher.start(),
                            latitudeStringMatcher.end())));
        }

        //Get the Longitude and Latitude from the extracted String
        Pattern longitudeStringPattern = Pattern.compile(RAW_LONGITUDE_STRING_REGEX_PATTERN);
        Matcher longitudeStringMatcher = longitudeStringPattern.matcher(coordinates);
        if (longitudeStringMatcher.find()) {
            extractedCoordinates.setLongitude(
                    getPrettyCoordinates(coordinates.substring(longitudeStringMatcher.start(),
                            longitudeStringMatcher.end())));
        }

        return extractedCoordinates;
    }

    /**
     * @param coordinates
     * @return
     */
    public static DecimalCoordinates resolveDecimalCoordinates(String coordinates) {
        //Get the actual coordinates from the string return from NOAA
        //30.000 N 90.000 W (30&#176;0'0" N 90&#176;0'0" W)
        DecimalCoordinates extractedCoordinates = new DecimalCoordinates();

        Pattern latitudeDecimalPattern = Pattern.compile(RAW_LATITUDE_DECIMAL_REGEX_PATTERN);
        Matcher latitudeDecimalMatcher = latitudeDecimalPattern.matcher(coordinates);
        if (latitudeDecimalMatcher.find()) {
            extractedCoordinates.setLatitude(
                    convertCoordinateToDecimal(coordinates.substring(latitudeDecimalMatcher.start(),
                            latitudeDecimalMatcher.end())));
        }

        Pattern longitudeDecimalPattern = Pattern.compile(RAW_LONGITUDE_DECIMAL_REGEX_PATTERN);
        Matcher longitudeDecimalMatcher = longitudeDecimalPattern.matcher(coordinates);
        if (longitudeDecimalMatcher.find()) {
            extractedCoordinates.setLongitude(
                    convertCoordinateToDecimal(coordinates.substring(longitudeDecimalMatcher.start(),
                            longitudeDecimalMatcher.end())));
        }

        return extractedCoordinates;
    }

    /**
     * @param coordinate String value like 42&#176;5'24" N
     * @return double value like 42.0900000
     */
    private static double convertCoordinateToDecimal(String coordinate) {
        //Convert 42.0900000 W to -42.0900000
        double decimalCoordinate = 0;

        // Negative value if South or West
        if (coordinate.trim().endsWith("S") || coordinate.trim().endsWith("W")) {
            decimalCoordinate = Double.valueOf(coordinate.trim().substring(0, coordinate.trim().length() - 2).trim());
            decimalCoordinate = 0 - decimalCoordinate;
        } else {
            String clearCoordinate = coordinate.trim().substring(0, coordinate.trim().length() - 2).trim();
            decimalCoordinate = Double.valueOf(clearCoordinate);
        }

        return decimalCoordinate;
    }

    private static String getPrettyCoordinates(String coordinates) {
        //replace the degree symbol
        return coordinates.replace(DEGREE_SYMBOL_PATTERN, DEGREE_SYMBOL);
    }

    //This will always return in METERS
    public static double getDistance(LatLng here, LatLng there) {
        return SphericalUtil.computeDistanceBetween(here, there);
    }
}
