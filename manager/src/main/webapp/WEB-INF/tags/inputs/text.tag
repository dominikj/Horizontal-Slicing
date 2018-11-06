<%@ attribute name="label" type="java.lang.String" required="true" %>
<%@ attribute name="bindPath" type="java.lang.String" required="true" %>
<%@ attribute name="readonly" type="java.lang.Boolean" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<spring:bind path="${bindPath}">
    <div class="form-group ${status.error ? "has-error" : ""}">
        <form:label cssClass="control-label" path="${bindPath}"
                    for="${bindPath}">${label}</form:label>
        <form:input readonly="${readonly}" path="${bindPath}" class="form-control" id="${bindPath}"/>
    </div>
</spring:bind>