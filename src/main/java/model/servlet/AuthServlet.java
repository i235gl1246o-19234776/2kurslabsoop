package model.servlet;

import jakarta.servlet.http.HttpServletResponse;
import model.entity.User;
import model.entity.UserRole;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public abstract class AuthServlet extends HttpServlet {

    protected User getAuthenticatedUser(HttpServletRequest req) {
        return (User) req.getAttribute("authenticatedUser");
    }

    protected UserRole getUserRole(HttpServletRequest req) {
        return (UserRole) req.getAttribute("userRole");
    }

    protected boolean isAdmin(HttpServletRequest req) {
        return getUserRole(req) == UserRole.ADMIN;
    }

    protected boolean isUser(HttpServletRequest req) {
        return getUserRole(req) == UserRole.USER;
    }

    protected boolean isAuthenticated(HttpServletRequest req) {
        return getAuthenticatedUser(req) != null;
    }

    protected void sendUnauthorized(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write("{\"error\":\"Unauthorized\"}");
    }

    protected void sendForbidden(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.getWriter().write("{\"error\":\"Access denied\"}");
    }
}