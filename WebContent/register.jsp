<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html><head><title>Register</title></head><body>
<h2>Register</h2>
<form method="post" action="register">
<label>Username:</label><br><input type="text" name="username" required><br>
<label>Email:</label><br><input type="email" name="email" required><br>
<label>Password:</label><br><input type="password" name="password" required><br><br>
<input type="submit" value="Register">
</form>
<p style="color:red;"><%= request.getAttribute("error")!=null?request.getAttribute("error"):"" %></p>
<p>Already have an account? <a href="login.jsp">Login here</a></p>
</body></html>