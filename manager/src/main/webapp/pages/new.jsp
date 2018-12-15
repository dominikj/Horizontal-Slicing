<%@ taglib prefix="navigation" tagdir="/WEB-INF/tags/navigation" %>
<%@ taglib prefix="inputs" tagdir="/WEB-INF/tags/inputs" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sprig" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>New/edit</title>

    <!-- Bootstrap Core CSS -->
    <link href="../vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <!-- MetisMenu CSS -->
    <link href="../vendor/metisMenu/metisMenu.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="../dist/css/sb-admin-2.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="../vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>

<body>

<div id="wrapper">
    <navigation:topNav/>
    <navigation:sidebar/>

    <div id="page-wrapper">
        <div class="row">
            <div class="col-lg-12">
                <c:choose>
                    <c:when test="${isNew}">
                        <h1 class="page-header">New</h1>
                    </c:when>
                    <c:otherwise>
                        <h1 class="page-header">Edit</h1>
                    </c:otherwise>
                </c:choose>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <c:choose>
                            <c:when test="${isNew}">
                                Create a new slice
                            </c:when>
                            <c:otherwise>
                                Edit an existing slice
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="panel-body">
                        <div class="row">
                            <div class="col-lg-8">
                                <sprig:url value="/save" var="saveUrl">
                                    <c:if test="${isNew ne null}">
                                        <spring:param name="isNew" value="${isNew}"/>
                                    </c:if>
                                </sprig:url>
                                <form:form role="form" method="POST" modelAttribute="slice" action="${saveUrl}">
                                <div class="form-group">
                                    <div class="panel">
                                        <inputs:text label="Slice name" bindPath="name"/>
                                        <inputs:text label="Description" bindPath="description"/>
                                    </div>
                                    <div class="panel panel-info">
                                        <div class="panel-heading">
                                            Client Application
                                        </div>
                                        <div class="panel-body">
                                            <inputs:text label="Docker image" bindPath="clientAppImageId"/>
                                            <inputs:text label="Port" bindPath="clientAppPublishedPort"/>
                                            <inputs:text label="Command" bindPath="clientAppCommand"/>
                                        </div>
                                    </div>
                                    <div class="panel panel-info">
                                        <div class="panel-heading">
                                            Server Application
                                        </div>
                                        <div class="panel-body">
                                            <inputs:text label="Docker image" bindPath="serverAppImageId"/>
                                            <inputs:text label="Port" bindPath="serverAppPublishedPort"/>
                                            <inputs:text label="Command" bindPath="serverAppCommand"/>
                                        </div>
                                    </div>
                                    <form:hidden path="id"/>
                                    <form:button type="submit" class="btn btn-default">${isNew ? 'Create' : 'Save'}</form:button>
                                    <a href="/" class="btn btn-default">Cancel</a>
                                    </form:form>
                                </div>
                            </div>
                            <!-- /.col-lg-6 (nested) -->
                        </div>
                        <!-- /.row (nested) -->
                    </div>
                    <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
    </div>
    <!-- /#page-wrapper -->

</div>
<!-- /#wrapper -->

<!-- jQuery -->
<script src="../vendor/jquery/jquery.min.js"></script>

<!-- Bootstrap Core JavaScript -->
<script src="../vendor/bootstrap/js/bootstrap.min.js"></script>

<!-- Metis Menu Plugin JavaScript -->
<script src="../vendor/metisMenu/metisMenu.min.js"></script>

<!-- Custom Theme JavaScript -->
<script src="../dist/js/sb-admin-2.js"></script>

</body>

</html>
