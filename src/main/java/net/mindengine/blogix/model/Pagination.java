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

import org.apache.commons.lang3.builder.HashCodeBuilder;


public class Pagination {
    private static final boolean INACTIVE = false;
    private static final Page ELLIPSIS = new Page( -1, INACTIVE);


    public static class Page {
        private int number;
        private boolean active;
        
        public Page() {
        }
        public Page(int number, boolean active) {
            this.number = number; 
            this.active = active;
        }
        
        public boolean isEllipsis() {
            return number < 0;
        }
        
        @Override
        public boolean equals(Object paramObject) {
            if (paramObject != null && paramObject instanceof Page) {
                Page page = (Page) paramObject;
                if (this.number == page.number && this.active == page.active) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return new HashCodeBuilder(34, 12)
            .append(number)
            .append(active)
            .toHashCode();
        }
        
        @Override
        public String toString() {
            if (isEllipsis()) {
                return "Ellipsis";
            }
            return String.format("Page(%d, %s)", number, active?"active":"inactive");
        }
        
        public boolean isActive() {
            return active;
        }
        public void setActive(boolean active) {
            this.active = active;
        }
        public int getNumber() {
            return number;
        }
        public void setNumber(int number) {
            this.number = number;
        }
    }

    public static List<Page> create(int firstPage, int lastPage, int range, int currentPage) {
        
        if (currentPage > lastPage) {
            currentPage = lastPage;
        }
        if (currentPage < firstPage) {
            currentPage = firstPage;
        }
        
        int rangeLeft = range;
        int rangeRight = range;
        
        if (currentPage <= firstPage + range) {
            rangeRight += range - (currentPage - firstPage);
        }
        else if (currentPage >= lastPage - range) {
            rangeLeft += range - (lastPage - currentPage);
        }
        
        int start = Math.max(currentPage - rangeLeft, firstPage + 1);
        int end = Math.min(currentPage + rangeRight, lastPage - 1);
        
        List<Page> pages = new LinkedList<Pagination.Page>();
        pages.add(page(firstPage, currentPage));
        
        if (start - firstPage > 1) {
            pages.add(ELLIPSIS);
        }
        
        for (int i = start; i <= end; i++) {
            pages.add(page(i, currentPage));
        }
        
        if (lastPage - end > 1) {
            pages.add(ELLIPSIS);
        }
        
        pages.add(page(lastPage, currentPage));
        
        return pages;
    }

    private static Page page(int firstPage, int currentPage) {
        return new Page(firstPage, currentPage != firstPage);
    }

}
