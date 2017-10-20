package eu.unicorn.helper.utils;

import eu.unicorn.cgmes.model.CgmesProfileDesc;
import eu.unicorn.cgmes.model.CgmesProfileType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class NameUtils {

    // 20140601T0000Z_ENTSO-E_EQ_BD_001.xml and 20140601T0000Z_ENTSO-E_TP_BD_001.xml
    // 20140601T1030Z_01_ELIA_SSH_001.xml TP SV
    // 20140601T1030Z_ELIA_EQ_001.xml
    public static String getFilenameXML(CgmesProfileDesc profileDesc) {
        Date date = profileDesc.getScenarioTime();
        LocalDateTime localDatetime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year = localDatetime.getYear();
        int month = localDatetime.getMonthValue();
        int day = localDatetime.getDayOfMonth();
        int hour = localDatetime.getHour();
        int minute = localDatetime.getMinute();

        String version = profileDesc.getVersion();

        // TODO resolve tso name
        String tso = "ELIA";
        // TODO resolve resolution
        String resolution = "01";
        switch (profileDesc.getCgmesProfileType()) {
            case EQUIPMENT:
                return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_" + tso + "_EQ_" + fillToThree(version);
            case TOPOLOGY:
                return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_" + resolution + "_" + tso + "_TP_" + fillToThree(version);
            case STATE_VARIABLES:
                return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_" + resolution + "_" + tso + "_SV_" + fillToThree(version);
            case STEADY_STATE_HYPOTHESIS:
                return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_" + resolution + "_" + tso + "_SSH_" + fillToThree(version);
            case EQUIPMENT_BOUNDARY:
                return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_ENTSOE-E_EQ_BD_" + fillToThree(version);
            case TOPOLOGY_BOUNDARY:
                return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_ENTSOE-E_TP_BD_" + fillToThree(version);
            default:
                return "undefined";
        }
    }

    // 20140601T0000Z_ENTSO-E_BD_001.zip
    public static String getFilenameZIPforBD(CgmesProfileDesc profileDesc) {
        Date date = profileDesc.getScenarioTime();
        LocalDateTime localDatetime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year = localDatetime.getYear();
        int month = localDatetime.getMonthValue();
        int day = localDatetime.getDayOfMonth();
        int hour = localDatetime.getHour();
        int minute = localDatetime.getMinute();

        String version = profileDesc.getVersion();

        if (profileDesc.getCgmesProfileType().equals(CgmesProfileType.EQUIPMENT_BOUNDARY) || profileDesc.getCgmesProfileType().equals(CgmesProfileType.TOPOLOGY_BOUNDARY)) {
            return String.valueOf(year) + fillToTwo(String.valueOf(month)) + fillToTwo(String.valueOf(day)) + "T" + String.valueOf(hour) + String.valueOf(minute) + "Z_ENTSOE-E_BD_" + fillToThree(version);
        } else {
            return "undefined";
        }
    }

    public static String getNameWithoutSuffix(String name) {
        String[] arrayOfNameAndSuffix = name.split("\\.");
        if (arrayOfNameAndSuffix.length != 2) {
            throw new IllegalArgumentException("Couldn't split to name and suffix. Name: " + name);
        } else {
            return arrayOfNameAndSuffix[0];
        }
    }

    public static String getSuffixFromName(String name) {
        String[] arrayOfNameAndSuffix = name.split("\\.");
        if (arrayOfNameAndSuffix.length != 2) {
            throw new IllegalArgumentException("Couldn't split to name and suffix. Name: " + name);
        } else {
            return arrayOfNameAndSuffix[1];
        }
    }

    public static String fillToThree(String string) {
        return ("000" + string).substring(string.length());
    }

    public static String fillToTwo(String string) {
        return ("00" + string).substring(string.length());
    }
}
