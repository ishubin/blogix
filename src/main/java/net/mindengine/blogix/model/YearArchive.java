package net.mindengine.blogix.model;

import java.util.LinkedList;
import java.util.List;

public class YearArchive {
    private int year;
    private List<MonthArchive> months;
    
    public YearArchive() {
    }
    public YearArchive(int year) {
        this.year = year;
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public List<MonthArchive> getMonths() {
        return months;
    }
    public void setMonths(List<MonthArchive> months) {
        this.months = months;
    }
    
    public void addMonth(MonthArchive monthArchive) {
        if (months == null) {
            months = new LinkedList<MonthArchive>();
        }
        months.add(monthArchive);
    }

}
