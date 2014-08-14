package org.mbmg.udp.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rpomeroy on 4/26/14.
 */
public class Record {

    // stationId_channel value timestamp
    private static String GRAPHITE_FORMAT = "%s.%s.%s %f %s\n";
    private static ZoneId UTC = ZoneId.of("UTC");
    private static Map<String,String> channelCodeToName = new HashMap<>();

    static {
        channelCodeToName.put("A00", "Humidity");
        channelCodeToName.put("A01", "Wind_Speed");
        channelCodeToName.put("A02", "Wind_Direction");
        channelCodeToName.put("A03", "Inverter_AC_Voltage");
        channelCodeToName.put("A04", "AC_Current");
        channelCodeToName.put("A05", "Wind_Controller_DC_Voltage");
        channelCodeToName.put("A06", "Solar_Controller_DC_Voltage");
        channelCodeToName.put("A07", "Station_Battery_DC_Voltage");
        channelCodeToName.put("A08", "Battery_Bus_DC_Voltage");
        channelCodeToName.put("A09", "Active_Power");
        channelCodeToName.put("A10", "Reactive_Power");
        channelCodeToName.put("A11", "Power_Factor");
        channelCodeToName.put("A12", "Frequency");
        channelCodeToName.put("A13", "Temperature_1");
        channelCodeToName.put("A14", "Temperature_2");
    }

    private Long recordNumber;
    private String recordType;
    private String stationID;
    private LocalDateTime timestamp;
    private Map<String, Double> channelData;

    public Record(Long recordNumber, String recordType, String stationID, LocalDateTime timestamp, Map<String,
            Double> channelData) {
        this.recordNumber = recordNumber;
        this.recordType = recordType;
        this.stationID = stationID;
        this.timestamp = timestamp;
        this.channelData = channelData;
    }
    
    public Record(String recordType, String stationID, LocalDateTime timestamp, Map<String,
            Double> channelData) {
        this.recordType = recordType;
        this.stationID = stationID;
        this.timestamp = timestamp;
        this.channelData = channelData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Record record = (Record) o;

        if (!channelData.equals(record.channelData)) return false;
        if (!recordNumber.equals(record.recordNumber)) return false;
        if (!recordType.equals(record.recordType)) return false;
        if (!stationID.equals(record.stationID)) return false;
        if (!timestamp.equals(record.timestamp)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = recordNumber.hashCode();
        result = 31 * result + recordType.hashCode();
        result = 31 * result + stationID.hashCode();
        result = 31 * result + timestamp.hashCode();
        result = 31 * result + channelData.hashCode();
        return result;
    }

    public Long getRecordNumber() {
        return recordNumber;
    }

    public String getRecordType() {
        return recordType;
    }

    public String getStationID() {
        return stationID;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Double> getChannelData() {
        return channelData;
    }

    public List<String> toGraphite() {
        // path value timestamp \n
        long epochTime = getEpochTime();
        List<String> data = new ArrayList();
        for (Map.Entry<String,Double> entry : this.channelData.entrySet()) {
            data.add(String.format(GRAPHITE_FORMAT,
                    getStationID(),
                    entry.getKey(),
                    channelCodeToName.getOrDefault(entry.getKey(), entry.getKey()),
                    entry.getValue(),
                    epochTime));
        }
        return data;
    }

    private long getEpochTime() {
        return this.getTimestamp().atZone(UTC).toEpochSecond();
    }
}
