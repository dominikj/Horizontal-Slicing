<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="slice" required="true" type="pl.mgr.hs.manager.dto.SliceDto" %>

<c:set var="tileConfiguration">
    <c:choose>
        <c:when test="${slice.working}">
            <div class="panel panel-green">
            <div class="panel-heading">
            <div class="row">
            <div class="col-xs-3">
                <i class="fa fa-tasks fa-5x"></i>
            </div>
        </c:when>
        <c:otherwise>
            <div class="panel panel-red">
            <div class="panel-heading">
            <div class="row">
            <div class="col-xs-3">
                <i class="fa fa-support fa-4x"></i>
            </div>
        </c:otherwise>
    </c:choose>
</c:set>
<div class="col-lg-3 col-md-6">
    ${tileConfiguration}
    <div class="col-xs-9 text-right">
        <div>${slice.name}</div>
        <div class="huge">${  slice.working ? slice.activeHosts : 'STOP'}</div>
        <div>${ slice.working ? 'active hosts' : 'slice is not working' }</div>
    </div>
</div>
</div>
<a href="/details/${slice.id}">
    <div class="panel-footer">
        <span class="pull-left">View Details</span>
        <span class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
        <div class="clearfix"></div>
    </div>
</a>
</div>
</div>