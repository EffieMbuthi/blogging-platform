package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import org.example.model.Comment;
import org.example.model.Post;
import org.example.model.PostEngagement;
import org.example.model.Tag;
import org.example.service.CommentService;
import org.example.service.PostService;
import org.example.service.ReportService;
import org.example.service.ReviewService;
import org.example.service.TagService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlogApp extends Application {
    private ReviewService reviewService = new ReviewService();
    private TagService tagService = new TagService();
    private ReportService reportService = new ReportService();

    private PostService postService = new PostService();
    private ListView<String> postListView = new ListView<>();

    private CommentService commentService = new CommentService();
    private ListView<String> commentListView = new ListView<>();

    private Map<String, Post> titleToPostMap = new HashMap<>();
    private Post selectedPost = null;

    private Label statusLabel = new Label();

    private int currentPage = 1;
    private final int PAGE_SIZE = 20;
    private Label pageLabel = new Label("Page 1");
    private Button prevButton = new Button("Previous");
    private Button nextButton = new Button("Next");

    private ComboBox<String> searchTypeBox = new ComboBox<>();

    private ListView<String> allTagsListView = new ListView<>();
    private ListView<String> postTagsListView = new ListView<>();
    private Map<String, Integer> tagNameToIdMap = new HashMap<>();
    private Label selectedPostForTagsLabel = new Label("No post selected.");

    private TableView<PostEngagement> reportTable = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        Tab postsTab = new Tab("Posts", buildPostsTab());
        postsTab.setClosable(false);

        Tab tagsTab = new Tab("Tags", buildTagsTab());
        tagsTab.setClosable(false);

        Tab analyticsTab = new Tab("Analytics", buildAnalyticsTab());
        analyticsTab.setClosable(false);

        TabPane tabPane = new TabPane(postsTab, tagsTab, analyticsTab);

        VBox root = new VBox(10, tabPane, statusLabel);
        Scene scene = new Scene(root, 650, 600);

        primaryStage.setTitle("Blogging Platform (PostgreSQL)");
        primaryStage.setScene(scene);
        primaryStage.show();

        loadPage();
        loadTags();
    }

    private VBox buildPostsTab() {
        searchTypeBox.getItems().addAll("Title", "Author", "Tag");
        searchTypeBox.setValue("Title");

        TextField titleField = new TextField();
        titleField.setPromptText("Post title");

        TextField bodyField = new TextField();
        bodyField.setPromptText("Post body");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by title..");

        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");

        Button createButton = new Button("Create Post");
        Button updateButton = new Button("Update Selected Post");
        Button deleteButton = new Button("Delete Selected");

        Button searchButton = new Button("Search");

        Button addCommentButton = new Button("Add Comment");
        Label commentsLabel = new Label("Comments:");

        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5);
        Button addReviewButton = new Button("Add Review");

        // Pre-fill fields and refresh comments/tags when a post is selected
        postListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selectedPost = titleToPostMap.get(newValue);
                titleField.setText(newValue);
                bodyField.setText(selectedPost.getBody());
                loadComments(selectedPost.getId());
                loadPostTags(selectedPost.getId());
            }
        });

        createButton.setOnAction(event -> {
            try {
                int authorId = 1;
                postService.createPost(authorId, titleField.getText(), bodyField.getText());
                statusLabel.setText("Post created successfully.");
                titleField.clear();
                bodyField.clear();
                loadPage();
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        updateButton.setOnAction(event -> {
            String selectedTitle = postListView.getSelectionModel().getSelectedItem();
            if (selectedTitle == null) {
                statusLabel.setText("Select a post first.");
                return;
            }
            try {
                int postId = titleToPostMap.get(selectedTitle).getId();
                postService.updatePost(postId, titleField.getText(), bodyField.getText());
                statusLabel.setText("Post updated successfully.");
                titleField.clear();
                bodyField.clear();
                loadPage();
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        searchButton.setOnAction(event -> performSearch(searchField.getText()));

        //listening to text field (#live filtering)
        searchField.textProperty().addListener((obs, oldValue, newValue) -> performSearch(newValue));

        nextButton.setOnAction(event -> {
            currentPage++;
            loadPage();
        });

        prevButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                loadPage();
            }
        });

        deleteButton.setOnAction(event -> {
            String selectedTitle = postListView.getSelectionModel().getSelectedItem();
            if (selectedTitle == null) {
                statusLabel.setText("Select a post first.");
                return;
            }
            try {
                int postId = titleToPostMap.get(selectedTitle).getId();
                postService.deletePost(postId);
                statusLabel.setText("Post deleted successfully.");
                titleField.clear();
                bodyField.clear();
                loadPage();
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        addCommentButton.setOnAction(event -> {
            String selectedTitle = postListView.getSelectionModel().getSelectedItem();
            if (selectedTitle == null) {
                statusLabel.setText("Select a post first.");
                return;
            }
            try {
                int postId = titleToPostMap.get(selectedTitle).getId();
                int userId = 1; // hardcoded, same simplification as post authorship
                commentService.createComment(postId, userId, commentField.getText());
                commentField.clear();
                loadComments(postId);
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        addReviewButton.setOnAction(event -> {
            String selectedTitle = postListView.getSelectionModel().getSelectedItem();
            if (selectedTitle == null) {
                statusLabel.setText("Select a post first.");
                return;
            }
            try {
                int postId = titleToPostMap.get(selectedTitle).getId();
                int userId = 1;
                reviewService.createReview(postId, userId, ratingSpinner.getValue());
                statusLabel.setText("Review added successfully.");
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        HBox addReviewRow = new HBox(10, ratingSpinner, addReviewButton);
        HBox searchRow = new HBox(10, searchTypeBox, searchField, searchButton);
        HBox buttonRow = new HBox(10, createButton, updateButton, deleteButton);
        HBox paginationRow = new HBox(10, prevButton, pageLabel, nextButton);
        HBox addCommentRow = new HBox(10, commentField, addCommentButton);

        return new VBox(10, searchRow, postListView, paginationRow, titleField, bodyField, buttonRow,
                commentsLabel, commentListView, addCommentRow, addReviewRow);
    }

    private VBox buildTagsTab() {
        TextField tagField = new TextField();
        tagField.setPromptText("New tag name");
        Button addTagButton = new Button("Add Tag");
        Button renameTagButton = new Button("Rename Selected Tag");
        Button deleteTagButton = new Button("Delete Selected Tag");
        Button attachTagButton = new Button("Attach to Selected Post");
        Button detachTagButton = new Button("Detach from Selected Post");

        addTagButton.setOnAction(event -> {
            try {
                tagService.createTag(tagField.getText());
                statusLabel.setText("Tag created successfully.");
                tagField.clear();
                loadTags();
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        renameTagButton.setOnAction(event -> {
            String selectedTagName = allTagsListView.getSelectionModel().getSelectedItem();
            if (selectedTagName == null) {
                statusLabel.setText("Select a tag first.");
                return;
            }
            TextInputDialog dialog = new TextInputDialog(selectedTagName);
            dialog.setTitle("Rename Tag");
            dialog.setHeaderText("Rename tag '" + selectedTagName + "'");
            dialog.setContentText("New name:");
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return;
            }
            try {
                int tagId = tagNameToIdMap.get(selectedTagName);
                tagService.renameTag(tagId, result.get());
                statusLabel.setText("Tag renamed successfully.");
                loadTags();
                if (selectedPost != null) {
                    loadPostTags(selectedPost.getId());
                }
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        deleteTagButton.setOnAction(event -> {
            String selectedTagName = allTagsListView.getSelectionModel().getSelectedItem();
            if (selectedTagName == null) {
                statusLabel.setText("Select a tag first.");
                return;
            }
            try {
                int tagId = tagNameToIdMap.get(selectedTagName);
                tagService.deleteTag(tagId);
                statusLabel.setText("Tag deleted successfully.");
                loadTags();
                if (selectedPost != null) {
                    loadPostTags(selectedPost.getId());
                }
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        attachTagButton.setOnAction(event -> {
            String selectedTagName = allTagsListView.getSelectionModel().getSelectedItem();
            if (selectedPost == null || selectedTagName == null) {
                statusLabel.setText("Select both a post (in the Posts tab) and a tag first.");
                return;
            }
            try {
                int tagId = tagNameToIdMap.get(selectedTagName);
                tagService.linkTagToPost(selectedPost.getId(), tagId);
                statusLabel.setText("Tag attached successfully.");
                loadPostTags(selectedPost.getId());
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        detachTagButton.setOnAction(event -> {
            String selectedTagName = postTagsListView.getSelectionModel().getSelectedItem();
            if (selectedPost == null || selectedTagName == null) {
                statusLabel.setText("Select both a post (in the Posts tab) and one of its tags first.");
                return;
            }
            try {
                int tagId = tagNameToIdMap.get(selectedTagName);
                tagService.unlinkTagFromPost(selectedPost.getId(), tagId);
                statusLabel.setText("Tag detached successfully.");
                loadPostTags(selectedPost.getId());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        HBox addTagRow = new HBox(10, tagField, addTagButton);
        HBox tagManageRow = new HBox(10, renameTagButton, deleteTagButton);
        HBox attachRow = new HBox(10, attachTagButton, detachTagButton);

        VBox allTagsBox = new VBox(5, new Label("All Tags"), allTagsListView, addTagRow, tagManageRow);
        VBox postTagsBox = new VBox(5, selectedPostForTagsLabel, new Label("Tags on selected post"),
                postTagsListView, attachRow);

        return new VBox(10, new HBox(20, allTagsBox, postTagsBox));
    }

    private VBox buildAnalyticsTab() {
        TableColumn<PostEngagement, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<PostEngagement, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("authorName"));

        TableColumn<PostEngagement, Integer> commentCountCol = new TableColumn<>("Comments");
        commentCountCol.setCellValueFactory(new PropertyValueFactory<>("commentCount"));

        TableColumn<PostEngagement, Double> avgRatingCol = new TableColumn<>("Avg Rating");
        avgRatingCol.setCellValueFactory(new PropertyValueFactory<>("avgRating"));

        TableColumn<PostEngagement, Integer> tagCountCol = new TableColumn<>("Tags");
        tagCountCol.setCellValueFactory(new PropertyValueFactory<>("tagCount"));

        reportTable.getColumns().addAll(titleCol, authorCol, commentCountCol, avgRatingCol, tagCountCol);

        Button refreshButton = new Button("Refresh Report");
        refreshButton.setOnAction(event -> loadReport());

        return new VBox(10, new Label("Post Engagement Report"), refreshButton, reportTable);
    }

    private void loadPage() {
        postListView.getItems().clear();
        titleToPostMap.clear();
        try {
            List<Post> posts = postService.getPostsPage(currentPage, PAGE_SIZE);
            for (Post post : posts) {
                postListView.getItems().add(post.getTitle());
                titleToPostMap.put(post.getTitle(), post);
            }
            int totalPages = postService.getTotalPages(PAGE_SIZE);
            pageLabel.setText("Page " + currentPage + " of " + totalPages);
            prevButton.setDisable(currentPage <= 1);
            nextButton.setDisable(currentPage >= totalPages);
        } catch (SQLException e) {
            postListView.getItems().add("Error loading posts: " + e.getMessage());
        }
    }

    private void loadComments(int postId) {
        commentListView.getItems().clear();
        try {
            List<Comment> comments = commentService.getCommentsForPost(postId);
            for (Comment comment : comments) {
                commentListView.getItems().add(comment.getBody());
            }
        } catch (SQLException e) {
            commentListView.getItems().add("Error loading comments: " + e.getMessage());
        }
    }

    private void performSearch(String keyword) {
        try {
            List<Post> results;
            if (searchTypeBox.getValue().equals("Author")) {
                results = postService.searchPostsByAuthor(keyword);
            } else if (searchTypeBox.getValue().equals("Tag")) {
                results = postService.searchPostsByTag(keyword);
            } else {
                results = postService.searchPosts(keyword);
            }
            postListView.getItems().clear();
            titleToPostMap.clear();
            for (Post post : results) {
                postListView.getItems().add(post.getTitle());
                titleToPostMap.put(post.getTitle(), post);
            }
        } catch (SQLException e) {
            statusLabel.setText("Search error: " + e.getMessage());
        }
    }

    private void loadTags() {
        allTagsListView.getItems().clear();
        tagNameToIdMap.clear();
        try {
            List<Tag> tags = tagService.getAllTags();
            for (Tag tag : tags) {
                allTagsListView.getItems().add(tag.getName());
                tagNameToIdMap.put(tag.getName(), tag.getId());
            }
        } catch (SQLException e) {
            statusLabel.setText("Error loading tags: " + e.getMessage());
        }
    }

    private void loadPostTags(int postId) {
        selectedPostForTagsLabel.setText("Selected post: " + selectedPost.getTitle());
        postTagsListView.getItems().clear();
        try {
            List<Tag> tags = tagService.getTagsForPost(postId);
            for (Tag tag : tags) {
                postTagsListView.getItems().add(tag.getName());
                tagNameToIdMap.put(tag.getName(), tag.getId());
            }
        } catch (SQLException e) {
            statusLabel.setText("Error loading tags for post: " + e.getMessage());
        }
    }

    private void loadReport() {
        try {
            List<PostEngagement> report = reportService.getPostEngagementReport();
            reportTable.setItems(FXCollections.observableArrayList(report));
        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
