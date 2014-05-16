package org.mbmg.udp.util;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Created by rpomeroy on 4/26/14.
 */
public class Record {
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
}
