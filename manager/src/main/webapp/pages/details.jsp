<%@ taglib prefix="navigation" tagdir="/WEB-INF/tags/navigation" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Details</title>

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

<c:set var="workingStatus">
    <c:choose>
        <c:when test="${slice.working}">
            <div class="alert alert-success">
                Working
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert alert-danger">
                Stopped
            </div>
        </c:otherwise>
    </c:choose>
</c:set>

<body>

<div id="wrapper">

    <!-- Navigation -->
    <navigation:topNav/>
    <navigation:sidebar/>
    <!-- Page Content -->
    <div id="page-wrapper">
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-12">
                    <h1 class="page-header">Slice details</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
        </div>
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        ${slice.name}
                    </div>
                    <!-- .panel-heading -->
                    <div class="panel-body">
                        <dt>Status</dt>
                        <dd>${workingStatus}</dd>
                        <div class="panel-group" id="accordion">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a data-toggle="collapse" data-parent="#accordion"
                                           href="#collapseOne">General information</a>
                                    </h4>
                                </div>
                                <div id="collapseOne" class="panel-collapse collapse in">
                                    <div class="panel-body">
                                        <dl>
                                            <dt>Name</dt>
                                            <dd>${slice.name}</dd>
                                            <dt>Description</dt>
                                            <dd>${slice.description}</dd>
                                            <dt>Manager host name</dt>
                                            <dd>${slice.managerHostName}</dd>
                                            <dt>Manager host address</dt>
                                            <dd>${slice.managerHostAddressInternal} (internal) <br/>
                                                ${slice.managerHostAddressExternal} (external)
                                            </dd>
                                            <dt>Join token</dt>
                                            <dd>${slice.joinToken}</dd>
                                        </dl>
                                    </div>
                                </div>
                            </div>
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a data-toggle="collapse" data-parent="#accordion"
                                           href="#collapseThree">Service</a>
                                    </h4>
                                </div>
                                <div id="collapseThree" class="panel-collapse collapse">
                                    <div class="panel-body">
                                        <div class="panel panel-info">
                                            <div class="panel-heading">
                                                Client Application
                                            </div>
                                            <div class="panel-body">
                                                <dl>
                                                    <dt>Docker image</dt>
                                                    <dd>${slice.clientApplication.image}</dd>
                                                    <dt>Exposed ports</dt>
                                                    <dd>${slice.clientApplication.publishedPorts}</dd>
                                                    <dt>Command</dt>
                                                    <dd>${slice.clientApplication.command}</dd>
                                                </dl>
                                            </div>
                                        </div>
                                        <div class="panel panel-info">

                                            <div class="panel-heading">
                                                Server Application
                                            </div>
                                            <div class="panel-body">
                                                <dl>
                                                    <dt>Docker image</dt>
                                                    <dd>${slice.serverApplication.image}</dd>
                                                    <dt>Exposed ports</dt>
                                                    <dd>${slice.serverApplication.publishedPorts}</dd>
                                                    <dt>Address</dt>
                                                    <dd>${slice.serverApplication.ipAddress}</dd>
                                                    <dt>Command</dt>
                                                    <dd>${slice.serverApplication.command}</dd>
                                                </dl>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a data-toggle="collapse" data-parent="#accordion"
                                           href="#collapseTwo">Connected hosts</a>
                                    </h4>
                                </div>
                                <div id="collapseTwo" class="panel-collapse collapse">
                                    <div class="panel-body">
                                        <table width="100%" class="table table-striped table-bordered table-hover"
                                               id="dataTables-example">
                                            <thead>
                                            <tr>
                                                <th>Host</th>
                                                <th>Address</th>
                                                <th>Replication status</th>
                                                <th>Replication info</th>
                                                <th>State</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${slice.hosts}" var="host">
                                                <tr class="odd gradeX">
                                                    <td>${host.name}</td>
                                                    <td>${host.address}</td>
                                                    <td class="center">${host.replicationStatus}</td>
                                                    <td>${host.replicationInfo}</td>
                                                    <td class="center">${host.state}</td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a data-toggle="collapse" data-parent="#accordion"
                                           href="#collapseFour">ACL</a>
                                    </h4>
                                </div>
                                <div id="collapseFour" class="panel-collapse collapse">
                                    <div class="panel-body">
                                        <div class="alert alert-warning"><i>Not implemented yet</i></div>
                                    </div>
                                </div>
                            </div>
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a data-toggle="collapse" data-parent="#accordion"
                                           href="#collapseFive">Actions</a>
                                    </h4>
                                </div>
                                <div id="collapseFive" class="panel-collapse collapse">
                                    <div class="panel-body">
                                        <c:if test="${slice.working}">
                                            <spring:url value="/actions/restart" var="restartUrl">
                                                <spring:param name="sliceId" value="${slice.id}"/>
                                            </spring:url>
                                            <a href="${restartUrl}" class="btn btn-warning">Restart</a>
                                        </c:if>
                                        <spring:url value="/actions/remove" var="removeUrl">
                                            <spring:param name="sliceId" value="${slice.id}"/>
                                        </spring:url>
                                        <a href="${removeUrl}" class="btn btn-danger">Remove</a>

                                        <c:choose>
                                            <c:when test="${not slice.working}">
                                                <spring:url value="/actions/start" var="startUrl">
                                                    <spring:param name="sliceId" value="${slice.id}"/>
                                                </spring:url>
                                                <a href="${startUrl}" class="btn btn-success">Start</a>
                                            </c:when>
                                            <c:otherwise>
                                                <spring:url value="/actions/stop" var="stopUrl">
                                                    <spring:param name="sliceId" value="${slice.id}"/>
                                                </spring:url>
                                                <a href="${stopUrl}" class="btn btn-danger">Stop</a>
                                            </c:otherwise>
                                        </c:choose>

                                        <a href="/edit/${slice.id}" class="btn btn-info">Edit</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- .panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.container-fluid -->
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
