package org.example.service;

import org.example.dao.TagDAO;
import org.example.model.Tag;
import java.sql.SQLException;
import java.util.List;

public class TagService {
    private TagDAO tagDAO;

    public TagService() {
        this.tagDAO = new TagDAO();
    }

    public void createTag(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be empty.");
        }
        tagDAO.createTag(name);
    }

    public List<Tag> getAllTags() throws SQLException {
        return tagDAO.findAllTags();
    }

    public void linkTagToPost(int postId, int tagId) throws SQLException {
        tagDAO.linkTagToPost(postId, tagId);
    }
}