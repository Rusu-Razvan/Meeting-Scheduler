<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html><head><title>Login</title></head><body>
<h2>Login</h2>
<form method="post" action="login">
<label>Email:</label><br><input type="email" name="email" required><br>
<label>Password:</label><br><input type="password" name="password" required><br><br>
<input type="submit" value="Login">
</form>
<p style="color:red;"><%= request.getAttribute("error")!=null?request.getAttribute("error"):"" %></p>
<p>No account? <a href="register.jsp">Register here</a></p>
</body></html>