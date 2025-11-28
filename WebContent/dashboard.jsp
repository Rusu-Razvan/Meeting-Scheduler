<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%
    // Redirect to login if not authenticated
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Dashboard – Online Meeting Scheduler</title>
  <style>
    :root{--bg:#f6f8fa;--primary:#2563eb;--text:#111827;--border:#d1d5db;}
    * { margin:0; box-sizing:border-box; font-family:system-ui,sans-serif; }
    body { background:var(--bg); color:var(--text); }
    header { display:flex; justify-content:space-between; align-items:center;
             padding:1rem; background:var(--primary); color:#fff; }
    button, .logout button { cursor:pointer; border:none; background:var(--primary);
                             color:#fff; padding:.5rem 1rem; border-radius:4px; }
    main { max-width:900px; margin:1rem auto; padding:0 1rem; }
    #notifications { margin-bottom:1.5rem; }
    #notifications ul { list-style:none; padding:0; }
    #notifications li { background:#fff; border:1px solid var(--border);
                         padding:.5rem; margin-bottom:.5rem; }
    .calendar { display:grid; grid-template-columns:repeat(7,1fr); gap:2px;
                background:var(--border); }
    .calendar div { background:#fff; min-height:75px; padding:4px;
                     cursor:pointer; position:relative; }
    .calendar .today { outline:2px solid var(--primary); }
    .calendar .selected { background:#e0e7ff; }
    #meetings { margin-top:2rem; }
    #meetings ul { list-style:none; padding:0; margin:0; }
    #meetings li { background:#fff; border:1px solid var(--border);
                   padding:.5rem; margin-bottom:.5rem; }
    #addBtn { display:block; margin-top:.5rem; }
  </style>
</head>
<body>
  <!-- Header -->
  <header>
    <h1>Hello, <%= session.getAttribute("username") %>!</h1>
    <form class="logout" method="post"
          action="<%= request.getContextPath() %>/logout">
      <button type="submit">Logout</button>
    </form>
  </header>

  <main>
    <!-- Notifications -->
    <section id="notifications">
      <h3>Notifications</h3>
      <ul id="notifList"><li>Loading…</li></ul>
    </section>

    <!-- Calendar -->
    <section>
  <div class="month-nav" style="display:flex; align-items:center; justify-content:center; gap:1rem; margin-bottom:.5rem;">
    <button id="prevMonth" style="font-size:1.2rem;">‹</button>
    <h2 id="monthLabel" style="margin:0;"></h2>
    <button id="nextMonth" style="font-size:1.2rem;">›</button>
  </div>
  <div class="calendar" id="calendar"></div>
</section>


    <!-- Meetings List -->
    <section id="meetings">
      <h3>Meetings for <span id="selectedDateLabel"></span></h3>
      <ul id="meetingList"></ul>
      <button id="addBtn">Add meeting</button>
    </section>
  </main>

<script>
  // Base context path
  var ctx = '<%= request.getContextPath() %>';

  // Format a Date object as YYYY-MM-DD
  function fmtDate(d) {
    var y = d.getFullYear();
    var m = String(d.getMonth() + 1).padStart(2, '0');
    var day = String(d.getDate()).padStart(2, '0');
    return y + '-' + m + '-' + day;
  }

  // State
  var today    = new Date();
  var selected = new Date(today);    // the day that’s highlighted
  var current  = new Date(today);    // the month being viewed
  var firstMonth = new Date(today.getFullYear(), today.getMonth(), 1);

  // DOM references
  var monthLabel    = document.getElementById('monthLabel');
  var cal           = document.getElementById('calendar');
  var selDateLabel  = document.getElementById('selectedDateLabel');
  var meetingList   = document.getElementById('meetingList');
  var notifList     = document.getElementById('notifList');
  var addBtn        = document.getElementById('addBtn');
  var prevBtn       = document.getElementById('prevMonth');
  var nextBtn       = document.getElementById('nextMonth');

  // Build calendar grid for a given year/month
  function buildCalendar(y, m) {
    cal.innerHTML = '';
    monthLabel.textContent =
      new Intl.DateTimeFormat('en', { month:'long', year:'numeric' })
      .format(new Date(y, m));

    var first = new Date(y, m, 1);
    var pad   = (first.getDay() + 6) % 7; // Monday-first
    var days  = new Date(y, m+1, 0).getDate();

    // padding blanks
    for (var i = 0; i < pad; i++) {
      cal.appendChild(document.createElement('div'));
    }
    // day cells
    for (var d = 1; d <= days; d++) {
      (function(day){
        var cell = document.createElement('div');
        cell.textContent = day;
        var date = new Date(y, m, day);
        if (fmtDate(date) === fmtDate(today)) {
          cell.classList.add('today');
        }
        cell.addEventListener('click', function(){
          selectDate(date, cell);
        });
        cal.appendChild(cell);
      })(d);
    }
  }

  // Handle date selection
  function selectDate(date, cell) {
    selected = date;
    Array.prototype.forEach.call(cal.children, function(c){
      c.classList.remove('selected');
    });
    cell.classList.add('selected');
    selDateLabel.textContent = fmtDate(date);
    loadMeetings(date);
  }

  // Load meetings via AJAX
  function loadMeetings(date) {
    meetingList.innerHTML = '<li>Loading…</li>';
    fetch(ctx + '/meetings?date=' + fmtDate(date))
      .then(function(r){ return r.ok ? r.json() : []; })
      .then(function(data){
        meetingList.innerHTML = '';
        if (data.length === 0) {
          meetingList.innerHTML = '<li>No meetings</li>';
          return;
        }
        data.forEach(function(m){
          var li = document.createElement('li');
          li.innerHTML =
            '<a href="' + ctx + '/meetings?id=' + m.id + '">' +
              '<strong>' + m.title + '</strong>' +
            '</a><br>' +
            m.time + ' (' + m.duration + ' min)';
          meetingList.appendChild(li);
        });
      })
      .catch(function(){
        meetingList.innerHTML = '<li>Error loading meetings</li>';
      });
  }

  // Load notifications via AJAX
  function loadNotifications() {
    notifList.innerHTML = '<li>Loading…</li>';
    fetch(ctx + '/notifications')
      .then(r => r.ok ? r.json() : [])
      .then(data => {
        notifList.innerHTML = '';
        if (!data.length) {
          notifList.innerHTML = '<li>No new notifications</li>';
          return;
        }
        data.forEach(function(n) {
        	  var li   = document.createElement('li');
        	  // make the li a flex container
        	  li.style.display = 'flex';
        	  li.style.justifyContent = 'space-between';
        	  li.style.alignItems = 'center';

        	  // 1) message span
        	  var span = document.createElement('span');
        	  var d    = new Date(n.createdAt);
        	  span.textContent = d.toLocaleString() + ': ' + n.message;
        	  li.appendChild(span);

        	  // 2) delete button
        	  var btn = document.createElement('button');
        	  btn.textContent = 'Delete';
        	  btn.style.marginLeft = '1rem';
        	  btn.addEventListener('click', function() {
        	    fetch(ctx + '/notifications/delete', {
        	      method: 'POST',
        	      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        	      body: 'id=' + encodeURIComponent(n.id)
        	    }).then(function(r) {
        	      if (r.ok || r.status === 204) {
        	        li.remove();
        	      }
        	    });
        	  });
        	  li.appendChild(btn);

        	  notifList.appendChild(li);
        	});

      })
      .catch(() => {
        notifList.innerHTML = '<li>Error loading notifications</li>';
      });
  }

  // Disable or enable the Prev button based on current month
  function updateNavButtons() {
    if (
      current.getFullYear() < firstMonth.getFullYear() ||
      (current.getFullYear() === firstMonth.getFullYear() &&
       current.getMonth() <= firstMonth.getMonth())
    ) {
      prevBtn.disabled = true;
    } else {
      prevBtn.disabled = false;
    }
  }

  // “Add meeting” button handler
  addBtn.addEventListener('click', function(){
    window.location = ctx + '/meetings?action=add&date=' + fmtDate(selected);
  });

  // Prev month navigation
  prevBtn.addEventListener('click', function(){
    if (prevBtn.disabled) return;
    current.setMonth(current.getMonth() - 1);
    buildCalendar(current.getFullYear(), current.getMonth());
    // select the first of that month
    var idx = (new Date(current.getFullYear(), current.getMonth(), 1).getDay() + 6) % 7;
    selectDate(new Date(current.getFullYear(), current.getMonth(), 1), cal.children[idx]);
    updateNavButtons();
  });

  // Next month navigation
  nextBtn.addEventListener('click', function(){
    current.setMonth(current.getMonth() + 1);
    buildCalendar(current.getFullYear(), current.getMonth());
    var idx = (new Date(current.getFullYear(), current.getMonth(), 1).getDay() + 6) % 7;
    selectDate(new Date(current.getFullYear(), current.getMonth(), 1), cal.children[idx]);
    updateNavButtons();
  });

  // Initial render
  buildCalendar(current.getFullYear(), current.getMonth());
  // highlight today’s cell
  Array.prototype.forEach.call(cal.children, function(c){
    if (c.textContent == today.getDate()) {
      selectDate(today, c);
    }
  });
  updateNavButtons();

  // Load notifications after page load
  window.addEventListener('load', loadNotifications);
</script>


</body>
</html>
