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
package net.mindengine.blogix.tests;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import net.mindengine.blogix.model.Pagination;
import net.mindengine.blogix.model.Pagination.Page;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

public class PaginationTest {

    private static final int FIRST_PAGE = 1;
    private static final int LAST_PAGE = 50;
    private static final int CURRENT_PAGE_1 = 1;
    private static final boolean INACTIVE = false;
    private static final boolean ACTIVE = true;
    private static final int CURRENT_PAGE_2 = 2;
    private static final int CURRENT_PAGE_3 = 3;
    private static final int CURRENT_PAGE_4 = 4;
    private static final Page ELLIPSIS = page(-1, INACTIVE);
    private static final int RANGE_OF_2 = 2;
    private static final int CURRENT_PAGE_5 = 5;
    
    /**
     * Pagination should be:
     * [1] 2 3  
     */
    @Test
    public void paginationForFirstPageWithSmallPages() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, 3, RANGE_OF_2, CURRENT_PAGE_1);
        assertThat(pagination, Matchers.contains(
                page(1, INACTIVE),
                page(2, ACTIVE),
                page(3, ACTIVE)));
    }

    /**
     * Pagination should be:
     * [1] 2 3 4 5 ... 50 
     */
    @Test
    public void paginationForFirstPage() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, LAST_PAGE, RANGE_OF_2, CURRENT_PAGE_1);
        assertThat(pagination, Matchers.contains(
                page(1, INACTIVE),
                page(2, ACTIVE),
                page(3, ACTIVE),
                page(4, ACTIVE),
                page(5, ACTIVE),
                ELLIPSIS,
                page(50, ACTIVE)));
    }
    
    /**
     * Pagination should be:
     * 1 [2] 3 4 5 ... 50 
     */
    @Test
    public void paginationForSecondPage() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, LAST_PAGE, RANGE_OF_2, CURRENT_PAGE_2);
        assertThat(pagination, Matchers.contains(
                page(1, ACTIVE),
                page(2, INACTIVE),
                page(3, ACTIVE),
                page(4, ACTIVE),
                page(5, ACTIVE),
                ELLIPSIS,
                page(50, ACTIVE)));
    }
    
    /**
     * Pagination should be:
     * 1 2 [3] 4 5 ... 50 
     */
    @Test
    public void paginationForThirdPage() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, LAST_PAGE, RANGE_OF_2, CURRENT_PAGE_3);
        assertThat(pagination, Matchers.contains(
                page(1, ACTIVE),
                page(2, ACTIVE),
                page(3, INACTIVE),
                page(4, ACTIVE),
                page(5, ACTIVE),
                ELLIPSIS,
                page(50, ACTIVE)));
    }
    
    /**
     * Pagination should be:
     * 1 2 3 [4] 5 6 ... 50 
     */
    @Test
    public void paginationForPage4() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, LAST_PAGE, RANGE_OF_2, CURRENT_PAGE_4);
        assertThat(pagination, Matchers.contains(
                page(1, ACTIVE),
                page(2, ACTIVE),
                page(3, ACTIVE),
                page(4, INACTIVE),
                page(5, ACTIVE),
                page(6, ACTIVE),
                ELLIPSIS,
                page(50, ACTIVE)));
    }
    
    /**
     * Pagination should be:
     * 1 ... 3 4 [5] 6 7 ... 50 
     */
    @Test
    public void paginationForPage5() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, LAST_PAGE, RANGE_OF_2, CURRENT_PAGE_5);
        assertThat(pagination, Matchers.contains(
                page(1, ACTIVE),
                ELLIPSIS,
                page(3, ACTIVE),
                page(4, ACTIVE),
                page(5, INACTIVE),
                page(6, ACTIVE),
                page(7, ACTIVE),
                ELLIPSIS,
                page(50, ACTIVE)));
    }
    
    /**
     * Pagination should be:
     * 1 ... 44 45 [46] 47 48 ... 50 
     */
    @Test
    public void paginationForPage46() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, LAST_PAGE, RANGE_OF_2, 46);
        assertThat(pagination, Matchers.contains(
                page(1, ACTIVE),
                ELLIPSIS,
                page(44, ACTIVE),
                page(45, ACTIVE),
                page(46, INACTIVE),
                page(47, ACTIVE),
                page(48, ACTIVE),
                ELLIPSIS,
                page(50, ACTIVE)));
    }
    
    
    /**
     * Pagination should be:
     * 1 ... 45 46 [47] 48 49 50 
     */
    @Test
    public void paginationForPage47() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, LAST_PAGE, RANGE_OF_2, 47);
        assertThat(pagination, Matchers.contains(
                page(1, ACTIVE),
                ELLIPSIS,
                page(45, ACTIVE),
                page(46, ACTIVE),
                page(47, INACTIVE),
                page(48, ACTIVE),
                page(49, ACTIVE),
                page(50, ACTIVE)));
    }
    
    /**
     * Pagination should be:
     * 1 ... 46 47 [48] 49 50 
     */
    @Test
    public void paginationForPage48() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, LAST_PAGE, RANGE_OF_2, 48);
        assertThat(pagination, Matchers.contains(
                page(1, ACTIVE),
                ELLIPSIS,
                page(46, ACTIVE),
                page(47, ACTIVE),
                page(48, INACTIVE),
                page(49, ACTIVE),
                page(50, ACTIVE)));
    }
    
    /**
     * Pagination should be:
     * 1 ... 46 47 48 49 [50] 
     */
    @Test
    public void paginationForPage50() {
        List<Pagination.Page> pagination = Pagination.create(FIRST_PAGE, LAST_PAGE, RANGE_OF_2, 50);
        assertThat(pagination, Matchers.contains(
                page(1, ACTIVE),
                ELLIPSIS,
                page(46, ACTIVE),
                page(47, ACTIVE),
                page(48, ACTIVE),
                page(49, ACTIVE),
                page(50, INACTIVE)));
    }
    

    private static Pagination.Page page(int number, boolean active) {
        return new Pagination.Page(number, active);
    }
}
