<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.model.Appointment,com.example.model.Participant,com.example.model.User,com.example.dao.UserDAO"%>
<%
    Appointment appt = (Appointment) request.getAttribute("appointment");
    @SuppressWarnings("unchecked")
    java.util.List<Participant> participants =
      (java.util.List<Participant>) request.getAttribute("participants");
    int me = (Integer) session.getAttribute("userId");
    String ctx = request.getContextPath();
    String date = appt.getStartTime().toLocalDate().toString();
%>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title><%= appt.getTitle() %></title></head>
<body>
  <h1><%= appt.getTitle() %></h1>
  <p><strong>When:</strong> <%= appt.getStartTime() %> — <%= appt.getEndTime() %></p>
  <p><strong>Description:</strong> <%= appt.getDescription() %></p>

  <h2>Participants</h2>
  <table border="1" cellpadding="5">
  <tr>
    <th>User</th><th>Status</th><th>Comment</th><th>Action</th>
  </tr>
  <% for (Participant p : participants) {
       User u = UserDAO.getById(p.getUserId());
       boolean isCreator = (p.getUserId() == appt.getCreatorId());
       boolean isMe      = (p.getUserId() == me);
  %>
  <tr>
    <td><%= u.getUsername() %> (<%= u.getEmail() %>)</td>
    <!-- Status column stays whatever the DB says -->
    <td><%= p.getStatus() %></td>
    
    <!-- Comment column: CREATOR for the creator, otherwise real comment -->
    <td>
      <% if (isCreator) { %>
        <strong>CREATOR</strong>
      <% } else {
           out.print(p.getComment() != null ? p.getComment() : "");
         }
      %>
    </td>
    
    <!-- Action column:
         • nothing for the creator
         • form only for other participants when it’s “me”
         • dash for everyone else -->
    <td>
      <% if (isCreator) { %>
        &mdash;
      <% } else if (isMe) { %>
        <form action="<%= ctx %>/participant" method="post" style="display:inline">
          <input type="hidden" name="appointmentId" value="<%= appt.getId() %>"/>
          <select name="status">
            <option value="ACCEPTED"
              <%= "ACCEPTED".equals(p.getStatus()) ? "selected" : "" %>>
              Accept
            </option>
            <option value="DECLINED"
              <%= "DECLINED".equals(p.getStatus()) ? "selected" : "" %>>
              Decline
            </option>
          </select>
          <input type="text" name="comment"
                 value="<%= p.getComment() != null ? p.getComment() : "" %>"
                 placeholder="Your comment…"/>
          <button type="submit">Submit</button>
        </form>
      <% } else { %>
        &mdash;
      <% } %>
    </td>
  </tr>
  <% } %>
</table>
<%  // Only show to the creator
    boolean isCreator = (Integer)session.getAttribute("userId") == appt.getCreatorId();
    %>
<% if (isCreator) { %>
  <form action="<%= request.getContextPath() %>/meetings" method="post"
        onsubmit="return confirm('Are you sure you want to delete this meeting?');"
        style="margin-top:1rem;">
    <input type="hidden" name="action" value="delete"/>
    <input type="hidden" name="appointmentId" value="<%= appt.getId() %>"/>
    <button type="submit" style="background:#e53e3e;color:white;padding:.5rem 1rem;border:none;border-radius:4px;">
      Delete Meeting
    </button>
  </form>
<% } %>



  <p>
  <a href="<%= ctx %>/dashboard.jsp?date=<%= date %>">
    &larr; Back to calendar
  </a>
</p>
</body>
</html>
