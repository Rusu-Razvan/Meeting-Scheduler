<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%
if(session.getAttribute("userId")==null){
    response.sendRedirect(request.getContextPath()+"/login.jsp");
    return;
}
String date = (String)request.getAttribute("date");
%>
<!DOCTYPE html><html><head>
<meta charset="UTF-8"><title>New meeting</title>
<style>
body{font-family:system-ui,sans-serif;margin:0;display:grid;place-items:center;height:100vh;background:#f6f8fa;}
form{background:#fff;padding:1rem;border:1px solid #d1d5db;border-radius:6px;min-width:280px;}
label{display:block;margin-top:.5rem;}
button{margin-top:1rem;background:#2563eb;color:#fff;border:none;padding:.5rem 1rem;border-radius:4px;}
#participantsContainer {
    border: 1px solid #ccc;
    padding: 0.5rem;
    max-height: 200px;
    overflow-y: auto;
    background: #fff;
  }
  #participantsContainer div {
    margin-bottom: 0.25rem;
  }
</style></head><body>
<form action="${pageContext.request.contextPath}/meetings?action=add" method="post">
  <h2>Add meeting</h2>
  <input type="hidden" name="date" value="<%=date%>">
  <label>Title <input type="text" name="title" required></label>
  <label>Time  <input type="time" name="time" required></label>
  <label>Duration (minutes) <input type="number" name="duration" min="15" step="15" required></label>
  <label>Participants</label>
<div id="participantsContainer">
  <%
  // get the logged-in user’s ID as a primitive int
  int me = (Integer) session.getAttribute("userId");
  @SuppressWarnings("unchecked")
  java.util.List<com.example.model.User> users =
      (java.util.List<com.example.model.User>) request.getAttribute("userList");

  if (users != null) {
    for (com.example.model.User u : users) {
      // skip yourself
      if (u.getId() == me) {
        continue;
      }
%>
      <div>
        <label>
          <input type="checkbox" name="participants" value="<%= u.getId() %>"/>
          <%= u.getUsername() %> (<%= u.getEmail() %>)
        </label>
      </div>
<%
    }
  } else {
%>
    <div>No other users found.</div>
<%
  }
%>

</div>

  
  <label>Description <textarea name="description" rows="3"></textarea></label>
  <button type="submit">Save</button>
</form>
</body></html>
