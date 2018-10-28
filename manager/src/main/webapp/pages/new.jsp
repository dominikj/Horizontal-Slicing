<%@ taglib prefix="navigation" tagdir="/WEB-INF/tags/navigation" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>SB Admin 2 - Bootstrap Admin Theme</title>

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
                <h1 class="page-header">New</h1>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        Create a new slice
                    </div>
                    <div class="panel-body">
                        <div class="row">
                            <div class="col-lg-8">
                                <form:form role="form" method="POST" modelAttribute="slice" action="/new">
                                <div class="form-group">
                                    <div class="panel">
                                        <form:label path="name">Slice name</form:label>
                                        <form:input path="name" class="form-control"/>
                                    </div>
                                    <div class="panel panel-info">
                                        <div class="panel-heading">
                                            Client Application
                                        </div>
                                        <div class="panel-body">
                                            <form:label path="clientAppImageId">Docker image</form:label>
                                            <form:input path="clientAppImageId" class="form-control"/>
                                            <form:errors path="clientAppImageId" cssClass="form-group has-error" element="div"/>

                                            <form:label path="clientAppPublishedPort">Port</form:label>
                                            <form:input type="number" path="clientAppPublishedPort" class="form-control"/>
                                        </div>
                                    </div>
                                    <div class="panel panel-info">
                                        <div class="panel-heading">
                                            Server Application
                                        </div>
                                        <div class="panel-body">
                                            <form:label path="serverAppImageId">Docker image</form:label>
                                            <form:input path="serverAppImageId" class="form-control"/>
                                            <form:label path="serverAppPublishedPort">Port</form:label>
                                            <form:input type="number" path="serverAppPublishedPort" class="form-control"/>
                                        </div>
                                    </div>
                                    <form:button type="submit" class="btn btn-default">Create</form:button>
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
