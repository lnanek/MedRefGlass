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
<%@ page import="com.neatocode.medrefglass.model.cards.WelcomeCard" %>
<%
	String userId = com.google.glassware.AuthUtil.getUserId(request);
 	WelcomeCard.insert(request, userId);  
%>
<html>
	<head>
		<title>MedRefGlass</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	    <link href="static/css/style.css" rel="stylesheet" media="screen" />		
	</head>
	<body>
		<div class="header">
			<h1><a href="/">MedRefGlass</a></h1>
		</div>	
		<div>		
			<p>
				<% String flash = WebUtil.getClearFlash(request);
					if (flash != null) { %>
						Message: <%= flash %> <br /><br />
				<% } %>			
				Signing into this page adds a new card and contacts to your Glass timeline.<br/>
				Make sure you have an internet connection and <br/>
				you'll hear a notification sound when they arrive.<br />
				You can then swipe to the card and tap it for options, or share photos<br />
				to the contacts to take photo notes and perform face searches of your<br />
				records.<br />
				<img 
					alt="Tap Google Glass"
						height="180"
					src="static/images/tap.png" />		
				<img 
					alt="OK Glass menu screen"
						height="180"
						src="static/images/glass_01_menu.jpg" />	<br />
						
				<img 
					alt="Swipe forward"
						height="180"
					src="static/images/swipe_forward.png" />	
				<img 
					alt="OK Glass menu screen"
						height="180"
						src="static/images/glass_03_welcome_card.png" />	<br />	
				<img 
					alt="Setting the patient to work with"
						height="180"
					src="static/images/glass_04_set_patient.png" />	
				<img 
					alt="Adding a voice note"
						height="180"
						src="static/images/glass_08_adding_a_voice_note.png" />	<br />	
				<img 
					alt="Sharing"
						height="180"
					src="static/images/glass_13_sharing.png" />	
				<img 
					alt="Face Search contact"
						height="180"
						src="static/images/glass_14_sharing_face_search.png" />	<br />	
				<img 
					alt="Sharing"
						height="180"
					src="static/images/glass_13_sharing.png" />	
				<img 
					alt="Face Search contact"
						height="180"
						src="static/images/glass_15_sharing_to_patient_note.png" />	<br />				
			</p>
			
		</div>
			
		<div class="footer">
    	   	<form class="navbar-form pull-right" action="/signout" method="post">
          		<input type="submit" value="Sign Out" />
        	</form>		
        </div>
	</body>
</html>

