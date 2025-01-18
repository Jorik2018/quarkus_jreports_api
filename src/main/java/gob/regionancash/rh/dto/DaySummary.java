package gob.regionancash.rh.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class DaySummary implements Serializable {

    private String dayName;

    private List exceptions;

    private String entry;

    private String breaking;

    private String out;

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    private long entryDelaySeconds;

    private long breakDelaySeconds;

    private int day;

    private Date date;

    private String decision = "";

    private String remark = "";

    public int getDay() {
        return day;
    }

    public void setDay(int dayMonth) {
        this.day = dayMonth;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getBreaking() {
        return breaking;
    }

    public void setBreaking(String breaking) {
        this.breaking = breaking;
    }


    //repeated by month
    private String fullName;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    private int period;

    private String foul = "";

    private String particular = "";

    private String tolerance = "";

    public Date getDate() {
        return date;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getFoul() {
        return foul;
    }

    public void setFoul(String foul) {
        this.foul = foul;
    }

    public String getParticular() {
        return particular;
    }

    public void setParticular(String particular) {
        this.particular = particular;
    }

    public String getTolerance() {
        return tolerance;
    }

    public void setTolerance(String tolerance) {
        this.tolerance = tolerance;
    }

    public List getExceptions() {
        return exceptions;
    }

    public void setExceptions(List exceptions) {
        this.exceptions = exceptions;
    }

    public long getEntryDelaySeconds() {
        return entryDelaySeconds;
    }

    public void setEntryDelaySeconds(long entryDelaySeconds) {
        this.entryDelaySeconds = entryDelaySeconds;
    }

    public long getBreakDelaySeconds() {
        return breakDelaySeconds;
    }

    public void setBreakDelaySeconds(long breakDelaySeconds) {
        this.breakDelaySeconds = breakDelaySeconds;
    }

}
