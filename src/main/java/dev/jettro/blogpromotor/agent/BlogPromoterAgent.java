package dev.jettro.blogpromotor.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.domain.library.HasContent;
import com.embabel.agent.prompt.persona.Persona;
import com.embabel.common.ai.model.AutoModelSelectionCriteria;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.prompt.PromptContributionLocation;
import com.embabel.common.core.types.Timestamped;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

abstract class Personas {
    static final Persona EXTRACTOR = Persona.create(
            "Blog Extractor",
            "A diligent researcher who extracts the essence of blog posts with precision",
            "Concise",
            "Extract the main content of a blog post from a URL without any boilerplate or additional information.",
            "",
            PromptContributionLocation.BEGINNING
    );
    static final Persona WRITER = Persona.create(
            "Blog Promoter",
            "A marketing expert who loves to create engaging content for social media",
            "Formal",
            "Create short introduction for social media of a blog post that is engaging to readers.",
            "",
            PromptContributionLocation.BEGINNING
    );
    static final Persona REVIEWER = Persona.create(
            "Marketing Reviewer",
            "Social Media Marketing Expert",
            "Professional and insightful",
            "Help guide social media posts toward good engagement",
            "",
            PromptContributionLocation.BEGINNING
    );
}


record Post(String content, String originalUrl, String[] hashtags) {
}

record BlogPost(String blogPostUrl, String content, String[] imageUrls) {
}

record PostImage(String imageUrl, String reasonForThisChoice) {}

record ReviewedPost(
        Post post,
        PostImage postImage,
        String review,
        Persona reviewer
) implements HasContent, Timestamped {

    @Override
    @NonNull
    public Instant getTimestamp() {
        return Instant.now();
    }

    @Override
    @NonNull
    public String getContent() {
        return String.format("""
            # Social Media Post
            %s
            
            # Original URL
            %s
            
            # Tags
            %s
            
            # Image URL
            %s
            
            # Image Reason
            %s

            # Review
            %s

            # Reviewer
            %s, %s
            """,
                post.content(),
                post.originalUrl(),
                String.join(", ", post.hashtags()),
                postImage.imageUrl(),
                postImage.reasonForThisChoice(),
                review,
                reviewer.getName(),
                getTimestamp().atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
        ).trim();
    }
}


@Agent(description = "Fetch content from a blog post, generate a post for socials about the blog post, select the best image from the page, review it for engagement.",
        name = "Promote on socials Agent")
@Profile("!test")
public class BlogPromoterAgent {
    private final int postWordCount;
    private final int reviewWordCount;

    BlogPromoterAgent(
            @Value("${postWordCount:100}") int postWordCount,
            @Value("${reviewWordCount:100}") int reviewWordCount
    ) {
        this.postWordCount = postWordCount;
        this.reviewWordCount = reviewWordCount;
    }

    @Action(toolGroups = {"mcp-firecrawl"})
    BlogPost fetchBlogPost(UserInput userInput, OperationContext context) {
        return context.promptRunner()
                .withLlm(
                        LlmOptions.fromCriteria(AutoModelSelectionCriteria.INSTANCE)
                                .withTemperature(0.2) // Higher temperature for more creative output
                ).withPromptContributor(Personas.EXTRACTOR)
                .createObject(String.format("""
                                Fetch the content of the blog post from the URL that is provided by the user.
                                If the user does not provide a URL or if the URL is not valid, return an error message with the problem.
                                Provide the content without any boilerplate or additional information.
                                Extract all the image urls from the page and return them in a list.
                                
                                # User input
                                %s
                                """, userInput.getContent().trim()), BlogPost.class);
    }

    @Action
    Post craftPost(BlogPost blogPost, OperationContext context) {
        return context.promptRunner()
                .withLlm(
                        LlmOptions.fromCriteria(AutoModelSelectionCriteria.INSTANCE)
                                .withTemperature(0.5) // Higher temperature for more creative output
                ).withPromptContributor(Personas.WRITER)
                .createObject(String.format("""
                                Craft a short social post in %d words or less for the platform %s.
                                The post should be engaging and promote content of the blogpost.
                                Use only essential technical terms and avoid jargon.
                                Add the url from the original input to the response: %s.
                                Add three single word hashtags that are relevant to the blog post to the text.
                                
                                # Blog post content
                                %s
                                """,
                        postWordCount,
                        "LinkedIn", // Assuming LinkedIn as the platform, can be parameterized
                        blogPost.blogPostUrl(),
                        blogPost.content()
                ).trim(), Post.class);
    }

    @Action
    PostImage selectBestImage(BlogPost blogPost, OperationContext context) {
        return context.promptRunner()
                .withLlm(LlmOptions.fromCriteria(AutoModelSelectionCriteria.INSTANCE))
                .withPromptContributor(Personas.WRITER)
                .createObject(String.format("""
                                Select the best image from the provided list of images.
                                
                                The image should be relevant to the content and suitable for social media.
                                Provide a reason for the choice of the image.
                                
                                # Blog post content
                                %s
                                
                                # Image URLs
                                %s
                                """,
                        blogPost.content(),
                        String.join(", ", blogPost.imageUrls())
                ).trim(), PostImage.class);

    }

    @AchievesGoal(description = "The blog post content is fetched, the social post is crafted and reviewed by the Marketing Reviewer.")
    @Action
    ReviewedPost reviewPost(Post post, PostImage postImage, OperationContext context) {
        String review = context.promptRunner()
                .withLlm(LlmOptions.fromCriteria(AutoModelSelectionCriteria.INSTANCE))
                .withPromptContributor(Personas.REVIEWER)
                .generateText(String.format("""
                                You will be given a social media post to review.
                                Review it in %d words or less.
                                Assure the sentences are connected and the post is coherent.
                                Consider whether the post is engaging, relevant, and appropriate for the platform %s.
                                If the post is not appropriate, provide a reason why it is not appropriate.
                                Provide the original url in the result: %s.
                                
                                # Social Media Post
                                %s
                                """,
                        reviewWordCount,
                        "LinkedIn", // Assuming LinkedIn as the platform, can be parameterized
                        post.originalUrl(),
                        post.content()
                ).trim());

        return new ReviewedPost(
                post,
                postImage,
                review,
                Personas.REVIEWER
        );
    }
}
