package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import net.mindengine.blogix.Blogix;
import net.mindengine.blogix.markup.DummyMarkup;
import net.mindengine.blogix.markup.Markup;
import net.mindengine.blogix.markup.TextileMarkup;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MarkupAccTest {

    @Test
    public void markupImpl_shouldBe_configuredInProperties_and_byDefault_shouldUseTextile() throws Exception {
        Blogix blogix = new Blogix();
        Markup markup = blogix.getMarkup();
        assertThat("Markup should be", markup, is(notNullValue()));
        assertThat("Markup should be TextileMarkup", markup, is(instanceOf(TextileMarkup.class)));
    }
    
    @Test
    public void markupImpl_shouldBe_DummyMarkup_if_itIsNotConfigured_inProperties() throws Exception {
        Blogix blogix = new Blogix();
        blogix.getProperties().remove("markup.class");
        
        Markup markup = blogix.getMarkup();
        assertThat("Markup should be", markup, is(notNullValue()));
        assertThat("Markup should be TextileMarkup", markup, is(instanceOf(DummyMarkup.class)));
    }
    
    @Test(dataProvider = "provideTextileMarkupData")
    public void textileMarkup_shouldProcess_Textile_and_customBlocks(String description, String textileText, String expectedHtml) {
        TextileMarkup markup = new TextileMarkup();
        String processedText = markup.apply(textileText);
        assertThat(String.format("\"%s\" processed text should be", description), processedText, is(expectedHtml));
    }

    @DataProvider
    public Object[][] provideTextileMarkupData() {
        return new Object[][]{
                {"Strong", "This is *strong* text", "<p>This is <strong>strong</strong> text</p>"},
                {"Bold", "This is **bold** text", "<p>This is <b>bold</b> text</p>"},
                {"Header level 1", "This is \n\rh1. Header 1\n\r", "<p>This is </p><h1 id=\"Header1\">Header 1</h1>"},
                {"Header level 2", "This is \n\rh2. Header 2\n\r", "<p>This is </p><h2 id=\"Header2\">Header 2</h2>"},
                {"Header level 3", "This is \n\rh3. Header 3\n\r", "<p>This is </p><h3 id=\"Header3\">Header 3</h3>"},
                {"Image", "!/image.jpg!", "<p><img border=\"0\" src=\"/image.jpg\"/></p>"},
                {"Emphasized", "_italics_", "<p><em>italics</em></p>"},
                {"Italics", "__italics__", "<p><i>italics</i></p>"},
                {"Paragraph", "par1\n\r\n\rpar2", "<p>par1</p><p>par2</p>"},
                {"Single Blockquote", "Hi \n\rbq. its a blockquote\n\rnot bq", "<p>Hi </p><blockquote><p>its a blockquote</p></blockquote><p>not bq</p>"},
                {"Multi-line Blockquote", "Hi \n\rbq.. its a blockquote\n\ranother part", "<p>Hi </p><blockquote><p>its a blockquote</p><p>another part</p></blockquote>"},
                {"Link", "hi \"link text\":link_address", "<p>hi <a href=\"link_address\">link text</a></p>"},
                {"Table",   "||head1||head2||\n" +
                		    "|col1|col2|\n" +
                		    "|col3|col4|", 
                		    "<table><tr><th>head1</th><th>head2</th></tr><tr><td>col1</td><td>col2</td></tr><tr><td>col3</td><td>col4</td></tr></table>"},
                {"Should escape html", "<script>alert(1);</script> &", "<p>&lt;script&gt;alert(1);&lt;/script&gt; &amp;</p>"},
                {"Should allow html blocks", "<nohtml>\n@@\n<script>alert(1);</script>\n@@", "<p>&lt;nohtml&gt;</p><script>alert(1);</script>\n"},
                {"Code blocks", "<nohtml>\n$$ c++\n<script>\n\nalert(1);</script>\n$$", "<p>&lt;nohtml&gt;</p><code class=\"block\" data-language=\"c++\">&lt;script&gt;\n\nalert(1);&lt;/script&gt;\n</code>"},
                {"Code block without language", "<nohtml>\n$$\n<script>\n\nalert(1);</script>\n$$", "<p>&lt;nohtml&gt;</p><code class=\"block\">&lt;script&gt;\n\nalert(1);&lt;/script&gt;\n</code>"},
        };
        //TODO finish samples
    }
    
}
