package dev.jettro.blogpromotor.agent;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.testing.unit.FakeOperationContext;
import com.embabel.agent.testing.unit.LlmInvocation;
import com.embabel.agent.testing.unit.UnitTestUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BlogPromoterAgentTest {

    @Test
    public void testBlogPromoterAgent() {
        var agent = new BlogPromoterAgent(200, 400);
        var llmCall = UnitTestUtils.captureLlmCall(
                () -> agent.fetchBlogPost(
                        new UserInput("Write a Linkedin post for the url: https://jettro.dev", Instant.now())
                )
        );
        assertTrue(llmCall.getPrompt().contains("https://jettro.dev"), "Expected prompt to contain the url 'https://jettro.dev'");
    }

    @Test
    public void testCraftPost() {
        var agent = new BlogPromoterAgent(100, 100);
        var blogPost = new BlogPost(
                "https://example.com/blog",
                "This is a test blog post content about Java and Spring Boot.",
                new String[]{"https://example.com/image1.png", "https://example.com/image2.png"}
        );
        var llmCall = UnitTestUtils.captureLlmCall(() -> agent.craftPost(blogPost));
        assertTrue(llmCall.getPrompt().contains("Craft a short social post"), "Prompt should instruct crafting a social post");
        assertTrue(llmCall.getPrompt().contains("https://example.com/blog"), "Prompt should include the blog post URL");
        assertTrue(llmCall.getPrompt().contains("blog post content"), "Prompt should include blog post content section");
    }

    @Test
    public void testSelectBestImage() {
        var agent = new BlogPromoterAgent(100, 100);
        var blogPost = new BlogPost(
                "https://example.com/blog",
                "This is a test blog post content about Java and Spring Boot.",
                new String[]{"https://example.com/image1.png", "https://example.com/image2.png"}
        );
        var llmCall = UnitTestUtils.captureLlmCall(() -> agent.selectBestImage(blogPost));
        assertTrue(llmCall.getPrompt().contains("Select the best image"), "Prompt should instruct to select the best image");
        assertTrue(llmCall.getPrompt().contains("This is a test blog post content"), "Prompt should include blog post content");
        assertTrue(llmCall.getPrompt().contains("image1.png"), "Prompt should include the first image URL");
        assertTrue(llmCall.getPrompt().contains("image2.png"), "Prompt should include the second image URL");
    }

    @Test
    public void testReviewPost() {
        var agent = new BlogPromoterAgent(100, 50);
        var post = new Post(
                "This is a crafted social post about Java.",
                "https://example.com/blog",
                new String[]{"Java", "Spring", "Boot"});
        var postImage = new PostImage(
                "https://example.com/image1.png",
                "Relevant to the blog content."
        );
        var context = new FakeOperationContext();
        context.expectResponse("This is a review of the post.");

        ReviewedPost reviewedPost = agent.reviewPost(post, postImage, context);

        assertEquals("This is a review of the post.", reviewedPost.review(), "Expected review to match the mocked response");

        List<LlmInvocation> llmInvocations = context.getLlmInvocations();
        assertFalse(llmInvocations.isEmpty(), "Expected at least one LLM invocation for review");

        LlmInvocation llmCall = llmInvocations.getFirst();

        assertTrue(llmCall.getPrompt().contains("review"), "Prompt should instruct to review the post");
        assertTrue(llmCall.getPrompt().contains("LinkedIn"), "Prompt should mention the platform");
        assertTrue(llmCall.getPrompt().contains("This is a crafted social post about Java."), "Prompt should include the social post content");
        assertTrue(llmCall.getPrompt().contains("https://example.com/blog"), "Prompt should include the original URL");
    }


}
