<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>

<div class="user-panel">
  <div class="pull-left image">
    <img src="resources/dist/img/user2-160x160.jpg" class="img-circle" alt="User Image">
  </div>
  <div class="pull-left info">
    <p>TEST
    </p>
    <!-- Status -->
  </div>
</div>

<!-- search form (Optional) -->
<form action="our-client-search" method="post" class="sidebar-form">
  <div class="input-group">
    <input type="text" name="companyRegNo" class="form-control" placeholder="Search...">
              <span class="input-group-btn">
                <button type="submit" name="search" id="search-btn" class="btn btn-flat"><i class="fa fa-search"></i>
                </button>
              </span>
  </div>
</form>

</body>
</html>
