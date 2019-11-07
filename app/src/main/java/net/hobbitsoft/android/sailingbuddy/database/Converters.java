package net.hobbitsoft.android.sailingbuddy.database;


import com.google.gson.Gson;

import net.hobbitsoft.android.sailingbuddy.data.DecimalCoordinates;
import net.hobbitsoft.android.sailingbuddy.data.Moon;
import net.hobbitsoft.android.sailingbuddy.data.StringCoordinates;
import net.hobbitsoft.android.sailingbuddy.data.Sun;
import net.hobbitsoft.android.sailingbuddy.data.Tide;
import net.hobbitsoft.android.sailingbuddy.data.Wave;
import net.hobbitsoft.android.sailingbuddy.data.Wind;

import java.util.Date;

import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public static Long longFromDate(Date date) {
        if (date != null) {
            return date.getTime();
        } else {
            return null;
        }
    }

    @TypeConverter
    public static Date longToDate(Long value) {
        if (value != null) {
            return new Date(value);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static Wind windFromJSON(String json) {
        if (json != null && !json.isEmpty()) {
            return new Gson().fromJson(json, Wind.class);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String windToJSON(Wind wind) {
        if (wind == null) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.toJson(wind);
        }
    }

    @TypeConverter
    public static Wave waveFromJSON(String json) {
        if (json != null && !json.isEmpty()) {
            return new Gson().fromJson(json, Wave.class);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String waveTOJSON(Wave wave) {
        if (wave == null) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.toJson(wave);
        }
    }

    @TypeConverter
    public static Tide tideFromJSON(String json) {
        // return json.isEmpty() ? null : new Gson().fromJson(json, Tide.class);
        if (json != null && !json.isEmpty()) {
            return new Gson().fromJson(json, Tide.class);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String tideToJSON(Tide tide) {
        if (tide == null) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.toJson(tide);
        }
    }

    @TypeConverter
    public static Moon moonFromJSON(String json) {
        if (json != null && !json.isEmpty()) {
            return new Gson().fromJson(json, Moon.class);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String moonToJSON(Moon moon) {
        if (moon == null) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.toJson(moon);
        }
    }

    @TypeConverter
    public static Sun sunFromJSON(String json) {
        if (json != null && !json.isEmpty()) {
            return new Gson().fromJson(json, Sun.class);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String tideToJSON(Sun sun) {
        if (sun == null) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.toJson(sun);
        }
    }

    @TypeConverter
    public static StringCoordinates stringCoordinatesToJSON(String json) {
        if (json != null && !json.isEmpty()) {
            return new Gson().fromJson(json, StringCoordinates.class);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String StringCoordinates(StringCoordinates stringCoordinates) {
        if (stringCoordinates == null) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.toJson(stringCoordinates);
        }
    }

    @TypeConverter
    public static DecimalCoordinates decimalCoordinatesToJSON(String json) {
        if (json != null && !json.isEmpty()) {
            return new Gson().fromJson(json, DecimalCoordinates.class);
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String DecimalCoordinates(DecimalCoordinates decimalCoordinates) {
        if (decimalCoordinates == null) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.toJson(decimalCoordinates);
        }
    }
}
