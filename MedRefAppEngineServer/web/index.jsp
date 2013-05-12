<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><!doctype html>
<%@ page import="com.google.api.client.auth.oauth2.Credential" %>
<%@ page import="com.google.api.services.mirror.model.Contact" %>
<%@ page import="com.google.glassware.MirrorClient" %>
<%@ page import="com.google.glassware.WebUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.api.services.mirror.model.TimelineItem" %>
<%@ page import="com.google.api.services.mirror.model.Subscription" %>
<%@ page import="com.google.api.services.mirror.model.Attachment" %>
<%@ page import="com.google.api.services.mirror.model.Attachment" %>
<%@ page import="com.neatocode.medrefglass.model.cards.Contacts" %>
<html>
	<head>
		<title>MedRefGlass</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	    <link href="static/css/style.css" rel="stylesheet" media="screen" />		
	</head>
	<body>
		<div class="header">
				<img 
					alt="Tap Google Glass"
						height="180"
					src="static/images/medref_icon_512x512.png" />	
			<h1>MedRef for Glass</h1>
			<p>
				<% String flash = WebUtil.getClearFlash(request);
					if (flash != null) { %>
						Message: <%= flash %> <br />
				<% } %>
				Take your care expertise into the future with Google's new wearable computer, <b>Google Glass</b>
			</p>
		</div>
		
		<div>
				<img 
					alt="Tap Google Glass"
						height="180"
					src="static/images/tap.png" />		
				<img 
					alt="OK Glass menu screen"
						height="180"
						src="static/images/glass_01_menu.jpg" />	
				<br />	
				<img 
					alt="MedRef welcome card"
						height="180"
						src="static/images/glass_03_welcome_card.png" />	
				<img 
					alt="OK Glass menu screen"
						height="180"
						src="static/images/glass_14_sharing_face_search.png" />	
				<br />		
				<img 
					alt="MedRef welcome card"
						height="180"
						src="static/images/glass_15_sharing_to_patient_note.png" />	
				<img 
					alt="OK Glass menu screen"
						height="180"
						src="static/images/glass_11_transcibed_text_note.png" />	
				<br />		
			<p>
				MedRec adds timeline cards and sharing contacts to Google Glass heads up display. 
				These can be interacted with to lookup patient records by saying their name 
				or taking a picture of their face. Care givers can then append photo and transcribed 
				voice notes easily to get the most accessible and complete data possible applied 
				to the patient's treatment.
			</p>
		</div>
			
		</div>
		<div class="footer">
				<form action="get_started.jsp">
					<input type="submit" value="INSTALL"/>
				</form>
				<br /><br />
		</div>
	</body>
</html>
