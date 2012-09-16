package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.util.List;

import net.mindengine.blogix.db.Entry;
import net.mindengine.blogix.db.FileDb;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FileDbAccTest {

    private FileDb postDb;
    
    @BeforeClass
    public void init() throws Exception {
        postDb = new FileDb(new File(getClass().getResource("/test-db/posts/").toURI()));
    }
    
    @Test
    public void shouldGiveNullForUnexistentEntry() throws Exception {
        Entry entry = postDb.findById("2012-01-30-some-unexistent-entry");
        assertThat(entry, is(nullValue()));
    }
    
    @Test
    public void shouldFindExistentEntry() throws Exception {
        Entry entry = postDb.findById("2012-01-30-some-title");
        assertThat(entry, is(notNullValue()));
    }
    
    @Test
    public void shouldFindOnlyIdsForAllEntries() throws Exception {
        List<String> ids = postDb.findAllIds();
        assertThat(ids.size(), is(3));
        assertThat(ids.get(0), is("2012-01-30-some-title"));
        assertThat(ids.get(1), is("2012-02-01-some-title-2"));
        assertThat(ids.get(2), is("2012-03-02-some-title-3"));
    }
    
    @Test
    public void shouldFindAllEntries() throws Exception {
        List<Entry> entries = postDb.findAll();
        assertThat(entries.size(), is(3));
        assertThat(entries.get(0).id(), is("2012-01-30-some-title"));
        assertThat(entries.get(1).id(), is("2012-02-01-some-title-2"));
        assertThat(entries.get(2).id(), is("2012-03-02-some-title-3"));
    }
    
    @Test
    public void shouldFindOnlyIdsBySpecifiedIdRegexPattern() throws Exception {
        List<String> ids = postDb.findAllIds("2012-(02|03).*");
        assertThat(ids.size(), is(2));
        assertThat(ids.get(0), is("2012-02-01-some-title-2"));
        assertThat(ids.get(1), is("2012-03-02-some-title-3"));
    }
    
    @Test
    public void shouldFindEntriesBySpecifiedIdRegexPattern() throws Exception {
        List<Entry> entries = postDb.findAll("2012-(02|03).*");
        assertThat(entries.size(), is(2));
        assertThat(entries.get(0).id(), is("2012-02-01-some-title-2"));
        assertThat(entries.get(1).id(), is("2012-03-02-some-title-3"));
    }
    
    @Test
    public void shouldFindEntryAttachments() throws Exception {
        List<String> attachments =  postDb.findAttachments("2012-01-30-some-title");
        assertThat(attachments.size(), is(2));
        assertThat(attachments.get(0), is("2012-01-30-some-title.1.jpg"));
        assertThat(attachments.get(1), is("2012-01-30-some-title.2.png"));
    }
    
    @Test
    public void shouldLoadSimpleEntryInSpecifiedPath() throws Exception {
        Entry entry = postDb.findById("2012-01-30-some-title");
        assertThat(entry.id(), is("2012-01-30-some-title"));
        assertThat(entry.field("title"), is("Sample title"));
        assertThat(entry.field("sections"), is("Section 1, Section 2, Section 3"));
        assertThat(entry.body(), is("This is just a body\n---------------This is actually not a delimiter\n"));
        assertThat(entry.field("anotherField"), is("field value after body"));
        assertThat(entry.field("someUnexistentField"), is(nullValue()));
    }
    
    @Test
    public void shouldSearchOnContainingField() throws Exception {
        List<Entry> entries = postDb.findByFieldContaining("sections", "Section 1");
        assertThat(entries.size(), is(2));
        assertThat(entries.get(0).id(), is("2012-01-30-some-title"));
        assertThat(entries.get(1).id(), is("2012-02-01-some-title-2"));        
    }
    
    @Test(enabled = false)
    public void shouldMapEntriesToJavaClasses() throws Exception {
    }

}
