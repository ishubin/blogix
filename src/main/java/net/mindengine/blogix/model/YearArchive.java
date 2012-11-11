/*******************************************************************************
* Copyright 2012 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
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
