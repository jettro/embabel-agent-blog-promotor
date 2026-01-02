package dev.jettro.blogpromotor.agent;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.FakePromptRunner;
import com.embabel.agent.test.unit.LlmInvocation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BlogPromoterAgentTest {

    @Test
    public void testBlogPromoterAgent() {
        // Given
        var context = FakeOperationContext.create();
        var promptRunner = (FakePromptRunner) context.promptRunner();
        context.expectResponse(new BlogPost(
                "https://jettro.dev",
                "This is a blog post about Java and Spring Boot.",
                new String[]{"https://jettro.dev/image1.png", "https://jettro.dev/image2.png"})
        );
        var agent = new BlogPromoterAgent(200, 400);

        // When
        agent.fetchBlogPost(
                new UserInput("Write a Linkedin post for the url: https://jettro.dev", Instant.now()), context
        );

        // Then
        String prompt = promptRunner.getLlmInvocations().getFirst().getMessages().getFirst().getContent();
        assertTrue(prompt.contains("https://jettro.dev"), "Expected prompt to contain the url 'https://jettro.dev'");
    }

    @Test
    public void testCraftPost() {
        // Given
        var context = FakeOperationContext.create();
        var promptRunner = (FakePromptRunner) context.promptRunner();
        context.expectResponse(new Post("This is a test blog post content about Java and Spring Boot.",
                "https://example.com/blog",
                new String[]{"Java", "Spring", "Boot"}));
        var blogPost = new BlogPost(
                "https://example.com/blog",
                "This is a test blog post content about Java and Spring Boot.",
                new String[]{"https://example.com/image1.png", "https://example.com/image2.png"}
        );
        var agent = new BlogPromoterAgent(100, 100);

        // When
        agent.craftPost(blogPost, context);

        // Then
        String prompt = promptRunner.getLlmInvocations().getFirst().getMessages().getFirst().getContent();
        assertTrue(prompt.contains("Craft a short social post"), "Prompt should instruct crafting a social post");
        assertTrue(prompt.contains("https://example.com/blog"), "Prompt should include the blog post URL");
        assertTrue(prompt.contains("blog post content"), "Prompt should include blog post content section");
    }

    @Test
    public void testSelectBestImage() {
        // Given
        var context = FakeOperationContext.create();
        context.expectResponse(new PostImage("https://example.com/image1.png", "Most relevant image"));
        var promptRunner = (FakePromptRunner) context.promptRunner();
        var agent = new BlogPromoterAgent(100, 100);
        var blogPost = new BlogPost(
                "https://example.com/blog",
                "This is a test blog post content about Java and Spring Boot.",
                new String[]{"https://example.com/image1.png", "https://example.com/image2.png"}
        );

        // When
        agent.selectBestImage(blogPost, context);

        // Then
        String prompt = promptRunner.getLlmInvocations().getFirst().getMessages().getFirst().getContent();
        assertTrue(prompt.contains("Select the best image"), "Prompt should instruct to select the best image");
        assertTrue(prompt.contains("This is a test blog post content"), "Prompt should include blog post content");
        assertTrue(prompt.contains("image1.png"), "Prompt should include the first image URL");
        assertTrue(prompt.contains("image2.png"), "Prompt should include the second image URL");
    }

    @Test
    public void testReviewPost() {
        var agent = new BlogPromoterAgent(100, 50);
        var post = new Post(
                "This is a crafted social post about Java.",
                "https://example.com/blog",
                new String[]{"Java", "Spring", "Boot"});
        var context = new FakeOperationContext();
        context.expectResponse("This is a review of the post.");

        ReviewedPost reviewedPost = agent.reviewPost(post, context);

        assertEquals("This is a review of the post.", reviewedPost.review(), "Expected review to match the mocked response");

        List<LlmInvocation> llmInvocations = context.getLlmInvocations();
        assertFalse(llmInvocations.isEmpty(), "Expected at least one LLM invocation for review");

        String promptContent = llmInvocations.getFirst().getMessages().getFirst().getContent();

        assertTrue(promptContent.contains("review"), "Prompt should instruct to review the post");
        assertTrue(promptContent.contains("LinkedIn"), "Prompt should mention the platform");
        assertTrue(promptContent.contains("This is a crafted social post about Java."), "Prompt should include the social post content");
        assertTrue(promptContent.contains("https://example.com/blog"), "Prompt should include the original URL");
    }

    @Test
    public void testConstructSocialMediaPost() {
        var agent = new BlogPromoterAgent(100, 100);

        var post = new Post(
                "This is a crafted social post about Java.",
                "https://example.com/blog",
                new String[]{"Java", "Spring", "Boot"});

        var reviewedPost = new ReviewedPost(
                post,
                "This post is engaging and suitable for LinkedIn.",
                Personas.REVIEWER
        );
        var postImage = new PostImage(
                "https://example.com/image1.png",
                "A relevant image for the post."
        );
        var socialMediaPost = agent.constructSocialMediaPost(reviewedPost, postImage);
        assertNotNull(socialMediaPost, "SocialMediaPost should not be null");
        assertEquals(reviewedPost, socialMediaPost.post(), "ReviewedPost should match");
        assertEquals(postImage, socialMediaPost.postImage(), "PostImage should match");
    }

}
