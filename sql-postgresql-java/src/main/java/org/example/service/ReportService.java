package org.example.service;

import org.example.dao.ReportDAO;
import org.example.model.PostEngagement;
import java.sql.SQLException;
import java.util.List;

public class ReportService {
    private ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public List<PostEngagement> getPostEngagementReport() throws SQLException {
        return reportDAO.findPostEngagement();
    }
}
