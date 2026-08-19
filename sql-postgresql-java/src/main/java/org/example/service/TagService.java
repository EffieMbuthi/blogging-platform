package org.example.service;

import org.example.dao.TagDAO;
import org.example.model.Tag;
import java.sql.SQLException;
import java.util.List;

public class TagService {
    private static final int MAX_NAME_LENGTH = 50;

    private TagDAO tagDAO;

    public TagService() {
        this.tagDAO = new TagDAO();
    }

    public void createTag(String name) throws SQLException {
        String trimmed = validateName(name);
        Tag existing = tagDAO.findTagByNameIgnoreCase(trimmed);
        if (existing != null) {
            throw new IllegalArgumentException("A tag named '" + trimmed + "' already exists.");
        }
        tagDAO.createTag(trimmed);
    }

    public void renameTag(int tagId, String newName) throws SQLException {
        String trimmed = validateName(newName);
        Tag existing = tagDAO.findTagByNameIgnoreCase(trimmed);
        if (existing != null && existing.getId() != tagId) {
            throw new IllegalArgumentException("A tag named '" + trimmed + "' already exists.");
        }
        tagDAO.updateTagName(tagId, trimmed);
    }

    public void deleteTag(int tagId) throws SQLException {
        tagDAO.deleteTag(tagId);
    }

    public List<Tag> getAllTags() throws SQLException {
        return tagDAO.findAllTags();
    }

    public List<Tag> getTagsForPost(int postId) throws SQLException {
        return tagDAO.findTagsForPost(postId);
    }

    public void linkTagToPost(int postId, int tagId) throws SQLException {
        boolean alreadyAttached = tagDAO.findTagsForPost(postId).stream()
                .anyMatch(tag -> tag.getId() == tagId);
        if (alreadyAttached) {
            throw new IllegalArgumentException("That tag is already attached to this post.");
        }
        tagDAO.linkTagToPost(postId, tagId);
    }

    public void unlinkTagFromPost(int postId, int tagId) throws SQLException {
        tagDAO.unlinkTagFromPost(postId, tagId);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be empty.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Tag name must be " + MAX_NAME_LENGTH + " characters or fewer.");
        }
        return trimmed;
    }
}