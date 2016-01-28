<%@ include file="/includes/taglibs.jsp"%>
<script>
	function validateWorkFlowApprover(name,errorDivId) {
		document.getElementById("workFlowAction").value=name;
	    var approverPosId = document.getElementById("approverPositionId");
	    if(approverPosId) {
			var approver = approverPosId.options[approverPosId.selectedIndex].text; 
			document.getElementById("approverName").value= approver.split('~')[0];
		}     
	   return  onSubmit();
	}

	function validateWorkFlowApprover(name) {
	    document.getElementById("workFlowAction").value=name;
	    var approverPosId = document.getElementById("approverPositionId");
	    if(approverPosId && approverPosId.value != -1 && approverPosId.value != "") {
			var approver = approverPosId.options[approverPosId.selectedIndex].text; 
			document.getElementById("approverName").value= approver.split('~')[0];
		}   
	    if ((name=="Reject" || name=="reject")) {
	    	var approverComments = document.getElementById("approverComments").value;
	    	if (approverComments == null || approverComments == "") {
	    		alert("Please Enter Approver Remarks ");
				return false;
	    	}
		}
		<s:if test="%{getNextAction()!='END'}">
	    if((name=="Forward" || name=="forward") && approverPosId && (approverPosId.value == -1 || approverPosId.value == "")) {
	        alert("Please Select the Approver ");
			return false;
	    }
	   
	    </s:if>
	    return  onSubmit();
	}
</script>
<div class="buttonbottom" >
	<s:hidden id="workFlowAction" name="workFlowAction" />
	<table style="width: 100%;" >
		<tr>
			<td><s:iterator value="%{getValidActions()}" var="validAction">
					<s:if test="%{validAction!=''}">
						<s:submit type="submit" cssClass="buttonsubmit" value="%{validAction}"
							id="%{validAction}" name="%{validAction}"
							onclick="return validateWorkFlowApprover('%{validAction}','jsValidationErrors');" />
					</s:if>
				</s:iterator></td><td> <input type="button" name="button2" id="button2" value="Close"
				class="button" onclick="window.close();" /></td>
		</tr>
	</table>
</div>
